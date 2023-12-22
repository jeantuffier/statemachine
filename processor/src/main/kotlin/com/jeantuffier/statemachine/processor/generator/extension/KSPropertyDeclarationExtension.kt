package com.jeantuffier.statemachine.processor.generator.extension

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Returns [com.jeantuffier.statemachine.orchestrate.OrchestratedData] or
 * [com.jeantuffier.statemachine.orchestrate.OrchestratedPage] if the property uses one of those types, Unit otherwise.
 */
fun KSPropertyDeclaration.orchestrationType(): TypeName {
    val type = type.resolve()
    return when {
        type.isOrchestratedData() -> type.firstArgumentClassName()
        type.isOrchestratedPage() -> Page::class.asClassName().parameterizedBy(type.firstArgumentClassName())
        else -> Unit::class.asClassName()
    }
}

/**
 * Checks if a declaration is of type [com.jeantuffier.statemachine.orchestrate.OrchestratedData].
 */
fun KSPropertyDeclaration.isOrchestratedData() = type.resolve().isOrchestratedData()

/**
 * Checks if a declaration is of type [com.jeantuffier.statemachine.orchestrate.OrchestratedPage].
 */
fun KSPropertyDeclaration.isOrchestratedPage() = type.resolve().isOrchestratedPage()

/**
 * Checks if a declaration is not Unit.
 */
fun KSPropertyDeclaration.isNotUnit() = type.resolve().toClassName() != Unit::class.asClassName()

/**
 * Returns the "action" property value of a declaration annotated with
 * [com.jeantuffier.statemachine.orchestrate.Orchestrated] as a [KSType].
 */
fun KSPropertyDeclaration.findActionType(): KSType? =
    findOrchestratedAnnotation()
        ?.findArgumentValueByName("action")


/**
 * Returns the "action" property value of a declaration annotated with
 * [com.jeantuffier.statemachine.orchestrate.Orchestrated] as a [KSClassDeclaration].
 */
fun KSPropertyDeclaration.findActionDeclaration(): KSClassDeclaration? =
    findActionType()?.declaration?.closestClassDeclaration()

/**
 * Checks if a declaration is annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated], returns the
 * annotation if present or null.
 */
fun KSPropertyDeclaration.findOrchestratedAnnotation(): KSAnnotation? =
    annotations.firstOrNull { it.annotationType.resolve().isOrchestrated() }

/**
 * Checks if a declaration is annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated].
 */
fun KSPropertyDeclaration.hasOrchestratedAnnotation(): Boolean =
    annotations.any { it.annotationType.resolve().isOrchestrated() }

/**
 * Checks if a declaration is annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated], then search for
 * the "loadingStrategy" property and returns its value.
 */
fun KSPropertyDeclaration.loadingStrategy(): LoadingStrategy {
    val name = findOrchestratedAnnotation()
        ?.findArgumentValueByName("loadingStrategy")
        ?.toClassName()?.simpleName ?: ""
    return LoadingStrategy.valueOf(name)
}

/**
 * Returns the parameter to use in a reducer or state machine for a given declaration.
 */
fun KSPropertyDeclaration.generateOrchestratedParameter(error: ClassName): ParameterizedTypeName {
    val orchestration = findOrchestratedAnnotation()
    val action = orchestration?.findArgumentValueByName("action") ?: throw IllegalStateException()
    val orchestratorType = when (loadingStrategy()) {
        LoadingStrategy.SUSPEND -> OrchestratedUpdate::class.asClassName()
        LoadingStrategy.FLOW -> OrchestratedFlowUpdate::class.asClassName()
        else -> throw IllegalStateException()
    }

    val orchestrationType = orchestrationType()

    return orchestratorType.parameterizedBy(action.toClassName(), error, orchestrationType)
}

/**
 * Returns the parameter to use in a reducer or state machine constructor for a given declaration.
 */
fun KSPropertyDeclaration.generateOrchestratedParameterSpec(error: ClassName): ParameterSpec {
    val orchestrator = generateOrchestratedParameter(error)
    return ParameterSpec.builder(simpleName.asString(), orchestrator)
        .build()
}

/**
 * Returns the simple name value of a property with its first character in upper case.
 */
fun KSPropertyDeclaration.upperCaseSimpleName(): String =
    simpleName.asString().replaceFirstChar(Char::uppercaseChar)
