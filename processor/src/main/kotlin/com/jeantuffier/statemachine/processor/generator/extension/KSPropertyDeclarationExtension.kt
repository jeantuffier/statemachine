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

fun KSPropertyDeclaration.orchestrationType(): TypeName {
    val type = type.resolve()
    return when {
        type.isOrchestratedData() -> type.firstArgumentClassName()
        type.isOrchestratedPage() -> Page::class.asClassName().parameterizedBy(type.firstArgumentClassName())
        else -> Unit::class.asClassName()
    }
}

fun KSPropertyDeclaration.isOrchestratedData() = type.resolve().isOrchestratedData()

fun KSPropertyDeclaration.isOrchestratedPage() = type.resolve().isOrchestratedPage()

fun KSPropertyDeclaration.isNotUnit() = type.resolve().toClassName() != Unit::class.asClassName()

fun KSPropertyDeclaration.findTriggerType(): KSType? =
    findOrchestratedAnnotation()
        ?.findArgumentValueByName("trigger")

fun KSPropertyDeclaration.findTriggerDeclaration(): KSClassDeclaration? =
    findTriggerType()?.declaration?.closestClassDeclaration()

fun KSPropertyDeclaration.findOrchestratedAnnotation(): KSAnnotation? =
    annotations.firstOrNull { it.annotationType.resolve().isOrchestrated() }

fun KSPropertyDeclaration.hasOrchestratedAnnotation(): Boolean =
    annotations.any { it.annotationType.resolve().isOrchestrated() }

fun KSPropertyDeclaration.loadingStrategy(): LoadingStrategy {
    val name = findOrchestratedAnnotation()
        ?.findArgumentValueByName("loadingStrategy")
        ?.toClassName()?.simpleName ?: ""
    return LoadingStrategy.valueOf(name)
}

fun KSPropertyDeclaration.generateOrchestratedParameter(error: ClassName): ParameterizedTypeName {
    val orchestration = findOrchestratedAnnotation()
    val trigger = orchestration?.findArgumentValueByName("trigger") ?: throw IllegalStateException()
    val orchestratorType = when (loadingStrategy()) {
        LoadingStrategy.SUSPEND -> OrchestratedUpdate::class.asClassName()
        LoadingStrategy.FLOW -> OrchestratedFlowUpdate::class.asClassName()
        else -> throw IllegalStateException()
    }

    val orchestrationType = orchestrationType()

    return orchestratorType.parameterizedBy(trigger.toClassName(), error, orchestrationType)
}

fun KSPropertyDeclaration.generateOrchestratedParameterSpec(error: ClassName): ParameterSpec {
    val orchestrator = generateOrchestratedParameter(error)
    return ParameterSpec.builder(simpleName.asString(), orchestrator)
        .build()
}

fun KSPropertyDeclaration.upperCaseSimpleName(): String =
    simpleName.asString().replaceFirstChar(Char::uppercaseChar)
