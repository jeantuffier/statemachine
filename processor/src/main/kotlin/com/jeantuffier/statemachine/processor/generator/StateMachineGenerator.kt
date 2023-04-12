package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.statemachine.core.StateMachine
import com.jeantuffier.statemachine.orchestrate.Content
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedSideEffect
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.Page
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.coroutines.CoroutineContext

class StateMachineGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateStateMachine(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val annotation = classDeclaration.annotations.first {
            it.shortName.asString() == Orchestration::class.asClassName().simpleName
        }
        val baseName = annotation.arguments.first().value as String
        val stateClass = "${baseName}State"
        val actionClass = "${baseName}Action"
        val functionName = "${baseName}StateMachine"

        val actionClassType = ClassName(packageName, actionClass)
        val stateClassType = ClassName(packageName, stateClass)
        val returnType = StateMachine::class.asClassName().parameterizedBy(actionClassType, stateClassType)

        val error = classDeclaration.annotations.first().arguments[1].value as KSType

        val fileSpec = FileSpec.builder(packageName, functionName).apply {
            addImport("com.jeantuffier.statemachine.core", "StateMachine")
            addFunction(
                FunSpec.builder(functionName.replaceFirstChar(Char::lowercaseChar))
                    .addParameters(orchestratedParameters(classDeclaration, error.toClassName()))
                    .addParameters(sideEffectParameters(classDeclaration, error.toClassName()))
                    .addParameter(
                        ParameterSpec.builder("coroutineContext", CoroutineContext::class.asClassName())
                            .build(),
                    )
                    .returns(returnType)
                    .addStatement(
                        """
                            return StateMachine(
                                initialValue = $stateClass(),
                                coroutineContext = coroutineContext,
                                reducer = ${baseName.replaceFirstChar(Char::lowercaseChar)}Reducer(
                                    ${stateMachineReducerParameter(classDeclaration).joinToString(",\n")}
                                )
                            )
                        """.trimIndent(),
                    )
                    .build(),
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun orchestratedParameters(
        classDeclaration: KSClassDeclaration,
        error: ClassName,
    ): List<ParameterSpec> {
        return classDeclaration.getAllProperties()
            .filter(::isOrchestratedProperty)
            .map { generateOrchestratedParameter(it, error) }
            .toList()
    }

    private fun sideEffectParameters(
        classDeclaration: KSClassDeclaration,
        error: ClassName,
    ): List<ParameterSpec> {
        val sideEffects = classDeclaration.annotations.first().arguments[2].value as List<KSType>
        return sideEffects.map {
            val orchestrator = OrchestratedSideEffect::class.asClassName()
                .parameterizedBy(it.toClassName(), error)
            ParameterSpec.builder(it.lowerCaseSimpleName(), orchestrator)
                .build()
        }
    }

    private fun generateOrchestratedParameter(
        property: KSPropertyDeclaration,
        error: ClassName,
    ): ParameterSpec {
        val name = property.simpleName.asString()

        val arguments = property.annotations.first().arguments.toList()

        val trigger = arguments[0].value as KSType

        val loadingStrategy = (arguments[1].value as? KSType)?.toClassName()?.simpleName ?: "SUSPEND"
        val orchestratorType = when (loadingStrategy) {
            "SUSPEND" -> OrchestratedUpdate::class.asClassName()
            "FLOW" -> OrchestratedFlowUpdate::class.asClassName()
            else -> throw IllegalStateException()
        }

        val type = property.type.resolve()

        val orchestrationType: TypeName = if (type.toClassName() == Content::class.asClassName()) {
            type.arguments[0].type!!.resolve().toClassName()
        } else {
            Page::class.asClassName().parameterizedBy(type.arguments[0].type!!.resolve().toClassName())
        }

        val orchestrator =
            orchestratorType.parameterizedBy(trigger.toClassName(), error, orchestrationType)
        return ParameterSpec.builder(name, orchestrator)
            .build()
    }

    private fun reducerStatements(
        classDeclaration: KSClassDeclaration,
        actionClass: ClassName,
    ): List<String> {
        val contentStatements = classDeclaration.getAllProperties()
            .filter(::isOrchestratedProperty)
            .groupBy {
                val type = it.annotations.first().arguments[0].value as KSType
                type.toClassName()
            }.map {
                orchestratedStatement(actionClass, it)
            }
        val sideEffects = classDeclaration.annotations.first().arguments[2].value as List<KSType>
        val sideEffectStatements = sideEffects.map {
            "is ${actionClass.simpleName}.${it.toClassName().simpleName} -> on${it.toClassName().simpleName}(action, ${it.lowerCaseSimpleName()})"
        }
        return contentStatements + sideEffectStatements
    }

    private fun isOrchestratedProperty(property: KSPropertyDeclaration) =
        property.annotations.any { it.shortName.asString() == Orchestrated::class.java.simpleName }

    private fun orchestratedStatement(
        actionClass: ClassName,
        entry: Map.Entry<ClassName, List<KSPropertyDeclaration>>,
    ): String {
        val (trigger, properties) = entry
        return with(StringBuilder()) {
            append("is ${actionClass.simpleName}.${trigger.simpleName} -> {")
            if (properties.size > 1) {
                append("merge(")
            }
            properties.forEach {
                val functionName = it.simpleName.asString().replaceFirstChar(Char::uppercaseChar)
                append("load$functionName(action, ${it.simpleName.asString()})")
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

    private fun KSType.lowerCaseSimpleName(): String =
        toClassName().simpleName.replaceFirstChar(Char::lowercaseChar)

    private fun stateMachineReducerParameter(classDeclaration: KSClassDeclaration): List<String> {
        val orchestratedProperties = classDeclaration.getAllProperties()
            .filter(::isOrchestratedProperty)
            .map { it.simpleName.asString() }
            .toList()
        val sideEffects = classDeclaration.annotations.first().arguments[2].value as List<KSType>

        return orchestratedProperties + sideEffects.map {
            it.toClassName().simpleName.replaceFirstChar(Char::lowercaseChar)
        }
    }
}
