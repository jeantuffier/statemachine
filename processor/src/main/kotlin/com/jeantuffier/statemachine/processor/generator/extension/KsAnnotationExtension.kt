package com.jeantuffier.statemachine.processor.generator.extension

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType

fun KSAnnotation.isOrchestration() = annotationType.resolve().isOrchestration()

fun KSAnnotation.findArgumentValueByName(name: String): KSType? =
    arguments.firstOrNull { it.name?.asString() == name }
        ?.value as? KSType

fun KSAnnotation.findArgumentValuesByName(name: String): List<KSType> =
    arguments.firstOrNull { it.name?.asString() == "actions" }
        ?.value as? List<KSType>
        ?: emptyList()

fun KSAnnotation.isAction() = annotationType.resolve().isAction()
