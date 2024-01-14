package com.jeantuffier.generator.generator.extension

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.generator.GeneratorProcessor.Companion.PACKAGE_NAME
import com.squareup.kotlinpoet.ClassName

/**
 * Checks if an annotation is [com.jeantuffier.statemachine.orchestrate.Orchestration]
 */
fun KSAnnotation.isOrchestration() = annotationType.resolve().isOrchestration()

/**
 * Find the first element in the list of arguments that matches the given name and return its value.
 */
fun KSAnnotation.findArgumentValueByName(name: String): KSType? =
    arguments.firstOrNull { it.name?.asString() == name }
        ?.value as? KSType


/**
 * Find the first element in the list of arguments that matches the given name and return its values as a list.
 */
fun KSAnnotation.findArgumentValuesByName(name: String): List<KSType> =
    arguments.firstOrNull { it.name?.asString() == "actions" }
        ?.value as? List<KSType>
        ?: emptyList()

/**
 * Checks if an annotation is [com.jeantuffier.statemachine.orchestrate.Action]
 */
fun KSAnnotation.isAction() = annotationType.resolve().isAction()

/**
 * Checks if an annotation is [com.jeantuffier.statemachine.orchestrate.Feature]
 */
fun KSAnnotation.isFeature() = annotationType.resolve().isFeature()

/**
 * Checks if an annotation is [com.jeantuffier.statemachine.orchestrate.StateUpdater]
 */
fun KSAnnotation.isStateUpdater() = annotationType.resolve().isStateUpdater()

/**
 * Checks if the annotation is @With
 */
fun KSAnnotation.isWith() = shortName.asString() == ClassName(PACKAGE_NAME, "With").simpleName

/**
 * Checks if the annotation is @Update
 */
fun KSAnnotation.isUpdate() = annotationType.resolve().isUpdate()
