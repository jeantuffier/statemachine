package com.jeantuffier.statemachine.processor.generator.extension

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType

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
