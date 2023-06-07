package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.jeantuffier.statemachine.core.Reducer
import com.jeantuffier.statemachine.processor.generator.extension.actionsParameters
import com.jeantuffier.statemachine.processor.generator.extension.findActions
import com.jeantuffier.statemachine.processor.generator.extension.findArgumentValueByName
import com.jeantuffier.statemachine.processor.generator.extension.findOrchestrationAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.findTriggerDeclaration
import com.jeantuffier.statemachine.processor.generator.extension.hasOrchestratedAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.lowerCaseSimpleName
import com.jeantuffier.statemachine.processor.generator.extension.orchestratedParameters
import com.jeantuffier.statemachine.processor.generator.extension.orchestrationBaseName
import com.jeantuffier.statemachine.processor.generator.extension.packageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class ReducerGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateReducer(classDeclaration: KSClassDeclaration) {
        val baseName = classDeclaration.orchestrationBaseName()
        val packageName = classDeclaration.packageName(baseName)
        val actionClassName = ClassName(packageName, "${baseName}Action")
        val stateClassName = ClassName(packageName, "${baseName}State")
        val returnType = Reducer::class.asClassName().parameterizedBy(actionClassName, stateClassName)

        val error = classDeclaration.findOrchestrationAnnotation()
            ?.findArgumentValueByName("errorType") ?: return

        val fileSpec = FileSpec.builder(packageName, "${baseName}Reducer").apply {
            addImport("kotlinx.coroutines.flow", "merge")
            addImport("kotlinx.coroutines.flow", "flow")

            addFunction(
                FunSpec.builder(name.replaceFirstChar(Char::lowercaseChar))
                    .addParameters(classDeclaration.orchestratedParameters(error.toClassName()))
                    .addParameters(classDeclaration.actionsParameters(stateClassName))
                    .returns(returnType)
                    .addStatement(
                        """
                            return Reducer { action ->
                                when(action) {
                                    ${reducerStatements(classDeclaration, baseName, actionClassName).joinToString("\n")}
                                }
                            }
                        """.trimIndent(),
                    )
                    .build(),
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun reducerStatements(
        classDeclaration: KSClassDeclaration,
        baseName: String,
        actionClass: ClassName,
    ): List<String> {
        val contentStatements = classDeclaration.getAllProperties()
            .filter { it.hasOrchestratedAnnotation() }
            .groupBy { it.findTriggerDeclaration()!!.toClassName() }
            .map { orchestratedStatement(actionClass, baseName, it) }
        val actionStatements = classDeclaration.findActions().map {
            "is ${actionClass.simpleName}.${it.toClassName().simpleName} -> ${it.lowerCaseSimpleName()}(action)"
        }
        val actionHandledStatement =
            "is ${actionClass.simpleName}.EventHandled -> flow { emit { it.copy(event = null) } }"
        return contentStatements + actionStatements + actionHandledStatement
    }

    private fun orchestratedStatement(
        actionClass: ClassName,
        baseName: String,
        entry: Map.Entry<ClassName, List<KSPropertyDeclaration>>,
    ): String {
        val (trigger, properties) = entry
        logger.warn("properties.size > 1: ${properties.size > 1}")
        return with(StringBuilder()) {
            append("is ${actionClass.simpleName}.${trigger.simpleName} -> {")
            if (properties.size > 1) {
                append("merge(")
            }
            properties.forEach {
                val functionName = it.simpleName.asString().replaceFirstChar(Char::uppercaseChar)
                append("load$baseName$functionName(action, ${it.simpleName.asString()})")
                if (properties.size > 1) {
                    append(",")
                }
                append("\n")
            }
            if (properties.size > 1) {
                append(")")
            }
            append("}")
        }.toString()
    }
}
