package com.jeantuffier.generator.generator.extension

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.orchestrate.OrchestratedAction
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Returns the "baseName" value of a [com.jeantuffier.statemachine.orchestrate.Orchestration] annotation.
 */
fun KSClassDeclaration.orchestrationBaseName() = annotations
    .first { it.isOrchestration() }
    .arguments
    .first().value as String

/**
 * Create a package name by using baseName parameter as a suffix to the declaration type's package name.
 */
fun KSClassDeclaration.packageName(baseName: String) =
    packageName.asString() + ".${baseName.replaceFirstChar(Char::lowercase)}"

/**
 * Checks if a declaration has an annotation of type [com.jeantuffier.statemachine.orchestrate.Orchestration].
 */
fun KSClassDeclaration.findOrchestrationAnnotation() = annotations.firstOrNull { it.isOrchestration() }

/**
 * Returns a set of [com.jeantuffier.statemachine.orchestrate.Action] if the declarations has an annotation of type
 * [com.jeantuffier.statemachine.orchestrate.Orchestration].
 */
fun KSClassDeclaration.findActions() = findOrchestrationAnnotation()
    ?.findArgumentValuesByName("actions")
    ?.toSet() ?: emptySet()

/**
 * Checks if a declaration has an annotation of type [com.jeantuffier.statemachine.orchestrate.Action].
 */
fun KSClassDeclaration.hasActionAnnotation() = annotations
    .map { it.annotationType.resolve() }
    .any { it.isAction() }

/**
 * Generates the list of parameters to use in reducer or state machine constructor for each property annotated with
 * [com.jeantuffier.statemachine.orchestrate.Orchestrated].
 */
fun KSClassDeclaration.orchestratedParameters(error: ClassName): List<ParameterSpec> =
    getAllProperties().filter { it.hasOrchestratedAnnotation() }
        .map { it.generateOrchestratedParameterSpec(error) }
        .toList()

/**
 * Generates the list of parameters to use in reducer or state machine constructor for each class listed in the
 * "actions" property of a [com.jeantuffier.statemachine.orchestrate.Orchestration] annotation.
 */
fun KSClassDeclaration.actionsParameters(stateClassName: ClassName): List<ParameterSpec> =
    findOrchestrationAnnotation()?.findArgumentValuesByName("actions")
        ?.map {
            val stateUpdate = OrchestratedAction::class.asClassName()
                .parameterizedBy(it.toClassName(), stateClassName)
            ParameterSpec.builder(it.lowerCaseSimpleName(), stateUpdate)
                .build()
        } ?: emptyList()

/**
 * Returns the "implementationName" value of a [com.jeantuffier.statemachine.orchestrate.Feature] annotation.
 */
fun KSClassDeclaration.featureImplementationName() = annotations
    .first { it.isFeature() }
    .arguments
    .first().value as String?

/**
 * Returns true if any of the functions declared in a [com.jeantuffier.statemachine.orchestrate.Feature] interface
 * is annotated with [com.jeantuffier.statemachine.orchestrate.UseCase] and the use case function is marked with
 * suspend.
 */
fun KSClassDeclaration.hasCancellableUseCases() = getAllFunctions().any { it.isSuspendableUseCase() }
