package com.jeantuffier.statemachine.processor.generator.extension

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.orchestrate.OrchestratedAction
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

fun KSClassDeclaration.orchestrationBaseName() = annotations
    .first { it.isOrchestration() }
    .arguments
    .first().value as String

fun KSClassDeclaration.packageName(baseName: String) =
    packageName.asString() + ".${baseName.replaceFirstChar(Char::lowercase)}"

fun KSClassDeclaration.findOrchestrationAnnotation() = annotations.firstOrNull { it.isOrchestration() }

fun KSClassDeclaration.findActions() = findOrchestrationAnnotation()
    ?.findArgumentValuesByName("actions")
    ?.toSet() ?: emptySet()

fun KSClassDeclaration.hasActionAnnotation() = annotations
    .map { it.annotationType.resolve() }
    .any { it.isAction() }

fun KSClassDeclaration.orchestratedParameters(error: ClassName): List<ParameterSpec> =
    getAllProperties().filter { it.hasOrchestratedAnnotation() }
        .map { it.generateOrchestratedParameterSpec(error) }
        .toList()

fun KSClassDeclaration.actionsParameters(stateClassName: ClassName): List<ParameterSpec> =
    findOrchestrationAnnotation()?.findArgumentValuesByName("actions")
        ?.map {
            val stateUpdate = OrchestratedAction::class.asClassName()
                .parameterizedBy(it.toClassName(), stateClassName)
            ParameterSpec.builder(it.lowerCaseSimpleName(), stateUpdate)
                .build()
        } ?: emptyList()
