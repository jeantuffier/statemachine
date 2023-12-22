package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.jeantuffier.statemachine.core.StateUpdate
import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.processor.generator.extension.findArgumentValueByName
import com.jeantuffier.statemachine.processor.generator.extension.findOrchestrationAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.findActionType
import com.jeantuffier.statemachine.processor.generator.extension.generateOrchestratedParameter
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedData
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedPage
import com.jeantuffier.statemachine.processor.generator.extension.loadingStrategy
import com.jeantuffier.statemachine.processor.generator.extension.orchestrationBaseName
import com.jeantuffier.statemachine.processor.generator.extension.orchestrationType
import com.jeantuffier.statemachine.processor.generator.extension.packageName
import com.jeantuffier.statemachine.processor.generator.extension.upperCaseSimpleName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.Flow

/**
 * Generate helper functions used by reducers to load data into properties annotated with
 * [com.jeantuffier.statemachine.orchestrate.Orchestrated].
 */
class HelpersGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateHelpers(classDeclaration: KSClassDeclaration) {
        val baseName = classDeclaration.orchestrationBaseName()
        val packageName = classDeclaration.packageName(baseName)
        val errorClassName = classDeclaration.findOrchestrationAnnotation()
            ?.findArgumentValueByName("errorType")
            ?.toClassName() ?: return
        val viewStateClassName = ClassName(packageName, "${baseName}State")

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = "${baseName}Helpers",
        ).apply {
            addImport("arrow.core", "Either")
            addImport("com.jeantuffier.statemachine.core", "StateUpdate")
            addImport("com.jeantuffier.statemachine.orchestrate", "OrchestratedData")
            addImport("com.jeantuffier.statemachine.orchestrate", "OrchestratedPage")
            addImport("kotlin.random", "Random")
            addImport("kotlinx.coroutines.flow", "flow")
            addImport("kotlinx.coroutines.flow", "map")
            addImport("kotlinx.coroutines.flow", "onStart")

            classDeclaration.getAllProperties()
                .filter { it.isOrchestratedData() || it.isOrchestratedPage() }
                .forEach { property ->
                    addImport(
                        classDeclaration.packageName.asString(),
                        "events.CouldNotLoad${property.upperCaseSimpleName()}",
                    )
                    addFunction(
                        loadFunction(
                            property,
                            baseName,
                            viewStateClassName,
                            errorClassName,
                        ),
                    )
                }
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}


/**
 * Generate a function containing logic to load data.
 */
private fun loadFunction(
    orchestratedProperty: KSPropertyDeclaration,
    baseName: String,
    viewStateClass: ClassName,
    errorClass: ClassName,
): FunSpec {
    val name = orchestratedProperty.simpleName.asString()
    val action = orchestratedProperty.findActionType() ?: throw IllegalStateException()
    val orchestrator = orchestratedProperty.generateOrchestratedParameter(errorClass)
    val state = StateUpdate::class.asClassName().parameterizedBy(viewStateClass)
    val returnType = Flow::class.asTypeName().parameterizedBy(state)

    return FunSpec.builder("load$baseName${name.replaceFirstChar(Char::uppercase)}")
        .addModifiers(KModifier.SUSPEND, KModifier.INTERNAL)
        .addParameter(ParameterSpec.builder("input", action.toTypeName()).build())
        .addParameter(ParameterSpec.builder("orchestrator", orchestrator).build())
        .returns(returnType)
        .addCode(
            when (orchestratedProperty.loadingStrategy()) {
                LoadingStrategy.SUSPEND -> orchestratedUpdateCodeBlock(name, orchestratedProperty)
                LoadingStrategy.FLOW -> {
                    val orchestrationType = orchestratedProperty.orchestrationType()
                    orchestratedFlowUpdateCodeBlock(name, errorClass, orchestrationType, state, orchestratedProperty)
                }

                else -> throw IllegalStateException()
            },
        )
        .build()
}

/**
 * Returns the code block handling [com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate].
 */
private fun orchestratedUpdateCodeBlock(
    name: String,
    orchestratedProperty: KSPropertyDeclaration,
): String = """
    |return flow {
    |   emit { it.copy($name = it.$name.copy(isLoading = true)) }
    |   when (val result = orchestrator(input)) {
    |       is Either.Left -> ${onDataLeft("emit", name)}
    |       is Either.Right -> ${onDataRight("emit", orchestratedProperty)}
    |   }
    |}
""".trimMargin()

/**
 * Returns the code block handling [com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate].
 */
private fun orchestratedFlowUpdateCodeBlock(
    name: String,
    errorClassName: ClassName,
    pageClassName: TypeName,
    state: ParameterizedTypeName,
    orchestratedProperty: KSPropertyDeclaration,
): String = """
    |return orchestrator(input)
    |   .map<Either<$errorClassName, $pageClassName>, $state> { result ->
    |       when (result) {
    |           is Either.Left -> ${onDataLeft("StateUpdate", name)}
    |           is Either.Right -> ${onDataRight("StateUpdate", orchestratedProperty)}
    |       }
    |   }
    |   .onStart { 
    |       emit($state {
    |           it.copy($name = it.$name.copy(isLoading = true)) 
    |       })
    |   }
""".trimMargin()

/**
 * Returns the code block handling a left result.
 */
private fun onDataLeft(startFunction: String, name: String): String =
    """
        |$startFunction {
        |   it.copy(
        |       $name = it.$name.copy(isLoading = false),
        |       event = CouldNotLoad${name.replaceFirstChar(Char::uppercase)}(result.value)
        |   )
        |}
    """.trimMargin()

/**
 * Returns the code block handling a right result.
 */
private fun onDataRight(startFunction: String, property: KSPropertyDeclaration): String =
    when {
        property.isOrchestratedData() -> {
            """
            |$startFunction {
            |   it.copy(
            |      ${property.simpleName.asString()} = OrchestratedData(
            |          isLoading = false,
            |          value = result.value,
            |      )
            |   )
            |}
            """.trimMargin()
        }

        property.isOrchestratedPage() -> {
            """
            |$startFunction {
            |   val pageNumber = result.value.offset.value / input.limit.value
            |      it.copy(
            |      ${property.simpleName.asString()} = OrchestratedPage(
            |          available = result.value.available,
            |          isLoading = false,
            |          pages = it.${property.simpleName.asString()}.pages.toMutableMap().apply {
            |              this[pageNumber] = result.value.items
            |          }
            |      )
            |   )
            |}
            """.trimMargin()
        }

        else -> throw IllegalStateException()
    }
