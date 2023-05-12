package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.statemachine.core.StateUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.OrchestratedSideEffect
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.Page
import com.jeantuffier.statemachine.orchestrate.SideEffect
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.Flow

class HelpersGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {
    fun generateHelpers(classDeclaration: KSClassDeclaration) {
        val arguments = classDeclaration.annotations.first {
            it.shortName.asString() == Orchestration::class.asClassName().simpleName
        }.arguments
        val baseName = arguments[0].value as String
        val packageName = classDeclaration.packageName.asString() + ".${baseName.replaceFirstChar(Char::lowercase)}"
        val errorClass = arguments[1].value as KSType
        val viewStateClassName = ClassName(packageName, "${baseName}State")
        val errorClassName = errorClass.toClassName()

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = "${baseName}Helpers",
        ).apply {
            addImport("com.jeantuffier.statemachine.core", "StateUpdate")
            addImport("com.jeantuffier.statemachine.orchestrate", "AsyncData")
            addImport("com.jeantuffier.statemachine.orchestrate", "OrchestratedData")
            addImport("com.jeantuffier.statemachine.orchestrate", "OrchestratedPage")
            addImport("com.jeantuffier.statemachine.orchestrate", "orchestrate")
            addImport("com.jeantuffier.statemachine.orchestrate", "orchestratePaging")
            addImport("com.jeantuffier.statemachine.orchestrate", "orchestrateSideEffect")
            addImport("kotlin.random", "Random")
            addImport("kotlinx.coroutines.flow", "flowOf")
            addImport("kotlinx.coroutines.flow", "map")

            classDeclaration.getAllProperties()
                .filter { it.isData() || it.isPagingContent() }
                .forEach { property ->
                    addImport(classDeclaration.packageName.asString(), "sideeffects.${property.upperCaseSimpleName()}SideEffects")
                    addFunction(
                        loadFunction(
                            property,
                            baseName,
                            viewStateClassName,
                            errorClassName,
                            logger,
                        ),
                    )
                }

            val sideEffects = classDeclaration.annotations.first().arguments[2].value as List<KSType>
            sideEffects.forEach {
                addImport(classDeclaration.packageName.asString(), "sideeffects.${it.toClassName().simpleName}SideEffects")
                addFunction(
                    sideEffectFunction(
                        it.toClassName(),
                        viewStateClassName,
                        errorClassName,
                        baseName,
                        logger,
                    ),
                )
            }

            addFunction(onSideEffectHandled(viewStateClassName, baseName))
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}

private fun loadFunction(
    orchestratedProperty: KSPropertyDeclaration,
    baseName: String,
    viewStateClass: ClassName,
    errorClass: ClassName,
    logger: KSPLogger,
): FunSpec {
    val name = orchestratedProperty.simpleName.asString()
    val input = orchestratedProperty.annotations.first().arguments.first().value as KSType
    val state = StateUpdate::class.asClassName().parameterizedBy(viewStateClass)
    val returnType = Flow::class.asTypeName().parameterizedBy(state)
    val type = orchestratedProperty.type.resolve()
    val orchestrationType: TypeName = if (type.toClassName() == OrchestratedData::class.asClassName()) {
        type.arguments[0].type!!.resolve().toClassName()
    } else {
        Page::class.asClassName().parameterizedBy(type.arguments[0].type!!.resolve().toClassName())
    }
    val arguments = orchestratedProperty.annotations.first().arguments.toList()
    val trigger = arguments[0].value as KSType
    val loadingStrategy = (arguments[1].value as? KSType)?.toClassName()?.simpleName ?: "SUSPEND"
    val orchestratorType = when (loadingStrategy) {
        "SUSPEND" -> OrchestratedUpdate::class.asClassName()
        "FLOW" -> OrchestratedFlowUpdate::class.asClassName()
        else -> throw IllegalStateException()
    }
    val orchestrator = orchestratorType.parameterizedBy(trigger.toClassName(), errorClass, orchestrationType)

    val builder = FunSpec.builder("load$baseName${name.replaceFirstChar(Char::uppercase)}")
        .addModifiers(KModifier.SUSPEND, KModifier.INTERNAL)
        .returns(returnType)
        .addParameter(ParameterSpec.builder("input", input.toTypeName()).build())
        .addParameter(
            ParameterSpec.builder("orchestrator", orchestrator)
                .build(),
        )

    val orchestration = when {
        orchestratedProperty.isData() -> "return orchestrate(input, orchestrator)"
        orchestratedProperty.isPagingContent() -> "return orchestratePaging(input, orchestrator)"
        else -> throw IllegalStateException()
    }

    builder.addStatement(
        """
        $orchestration
            .map { newValue ->
                when (newValue) {
                    is AsyncData.Loading -> StateUpdate { it.copy($name = it.$name.copy(isLoading = true)) }
                    is AsyncData.Success -> StateUpdate {
                        ${updateStatement(orchestratedProperty)}
                    }
                    is AsyncData.Failure -> StateUpdate {
                       it.copy(
                            $name = it.$name.copy(isLoading = false),
                            sideEffects = it.sideEffects + ${name.replaceFirstChar(Char::uppercase)}SideEffects.CouldNotBeLoaded(Random.nextLong(), newValue.error)
                        )
                    }
                }
            }
        """.trimMargin(),
    )

    return builder.build()
}

private fun sideEffectFunction(
    trigger: ClassName,
    viewStateClass: ClassName,
    errorClass: ClassName,
    baseName: String,
    logger: KSPLogger,
): FunSpec {
    val name = trigger.simpleName
    val state = StateUpdate::class.asClassName().parameterizedBy(viewStateClass)
    val returnType = Flow::class.asTypeName().parameterizedBy(state)
    val orchestrator = OrchestratedSideEffect::class.asClassName()
        .parameterizedBy(trigger, errorClass)

    val builder = FunSpec.builder("on$baseName$name")
        .addModifiers(KModifier.SUSPEND, KModifier.INTERNAL)
        .returns(returnType)
        .addParameter(ParameterSpec.builder("input", trigger).build())
        .addParameter(
            ParameterSpec.builder("orchestrator", orchestrator)
                .build(),
        )

    builder.addStatement(
        """
        return orchestrateSideEffect(input, orchestrator)
            .map { newValue ->
                when (newValue) {
                    is AsyncData.Loading -> StateUpdate {
                        it.copy(
                            sideEffects = it.sideEffects + ${trigger.simpleName}SideEffects.Waiting(Random.nextLong())
                        )
                    }
                    is AsyncData.Success -> StateUpdate {
                        it.copy(
                            sideEffects = it.sideEffects + ${trigger.simpleName}SideEffects.Succeeded(Random.nextLong())
                        )
                    }
                    is AsyncData.Failure -> StateUpdate {
                        it.copy(
                            sideEffects = it.sideEffects + ${trigger.simpleName}SideEffects.Failed(Random.nextLong())
                        )
                    }
                }
            }
        """.trimMargin(),
    )

    return builder.build()
}

private fun KSPropertyDeclaration.isData(): Boolean =
    type.resolve().toClassName() == OrchestratedData::class.asClassName()

private fun KSPropertyDeclaration.isPagingContent(): Boolean =
    type.resolve().toClassName() == OrchestratedPage::class.asClassName()

private fun updateAvailability(property: KSPropertyDeclaration): String {
    return if (property.isPagingContent()) {
        "available = newValue.data.available,"
    } else {
        ""
    }
}

private fun updateStatement(property: KSPropertyDeclaration): String =
    when {
        property.isData() -> {
            """
                it.copy(
                    ${property.simpleName.asString()} = OrchestratedData(
                        isLoading = false,
                        value = newValue.data,
                    )
                )
            """.trimIndent()
        }

        property.isPagingContent() -> {
            """
                val pageNumber = newValue.data.offset.value / input.limit
                it.copy(
                    ${property.simpleName.asString()} = OrchestratedPage(
                        available = newValue.data.available,
                        isLoading = false,
                        pages = it.${property.simpleName.asString()}.pages.toMutableMap().apply {
                            this[pageNumber] = newValue.data.items
                        }
                    )
                )
            """.trimIndent()
        }

        else -> throw IllegalStateException()
    }

private fun onSideEffectHandled(
    viewStateClass: ClassName,
    baseName: String,
): FunSpec {
    return FunSpec.builder("on${baseName}SideEffectHandled")
        .addParameter(
            ParameterSpec.builder("sideEffect", SideEffect::class.asTypeName())
                .build(),
        )
        .addStatement(
            """
                return flowOf(
                    StateUpdate<${viewStateClass.simpleName}> { state ->
                        state.copy(sideEffects = state.sideEffects.filterNot { it.id == sideEffect.id })
                    }
                )
            """.trimIndent(),
        )
        .build()
}

private fun KSPropertyDeclaration.upperCaseSimpleName(): String =
    simpleName.asString().replaceFirstChar(Char::uppercaseChar)
