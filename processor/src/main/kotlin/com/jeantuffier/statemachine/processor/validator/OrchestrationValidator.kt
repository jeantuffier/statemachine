package com.jeantuffier.statemachine.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.AsyncData
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

class OrchestrationValidator {

    fun isValid(symbol: KSAnnotated, logger: KSPLogger): Boolean {
        return symbol is KSClassDeclaration &&
            symbol.validate() &&
            symbol.validateClassNames(logger) &&
            symbol.validateProperties(logger)
    }

    private fun KSClassDeclaration.validateClassNames(logger: KSPLogger): Boolean {
        val arguments = annotations
            .first { it.shortName.asString() == Orchestration::class.asClassName().simpleName }
            .arguments

        val baseName = arguments.first().value as String

        if (baseName.isEmpty()) {
            logger.error("\"baseName\" argument of @Orchestration cannot be empty.")
            return false
        }

        return true
    }

    private fun KSClassDeclaration.validateProperties(logger: KSPLogger): Boolean {
        val properties = getAllProperties()
        if (properties.toList().isEmpty()) {
            logger.error("An interface annotated with @Orchestration should contains at least one property.")
            return false
        }
        properties.forEach { property ->
            val propertySimpleName = property.type.resolve().toClassName().simpleName

            if (propertySimpleName == AsyncData::class.java.simpleName) {
                return validateOrchestrationProperty(property, logger)
            }
        }

        return true
    }

    private fun validateOrchestrationProperty(
        property: KSPropertyDeclaration,
        logger: KSPLogger,
    ): Boolean {
        val propertySimpleName = property.type.resolve().toClassName().simpleName
        val propertyAnnotations = property.annotations.toList()

        if (propertyAnnotations.size != 1) {
            logger.error("Each property inside an interface annotated with @Orchestration should only be annotated with one @Orchestrated.")
            return false
        }

        val annotation = propertyAnnotations.first()
        if (annotation.shortName.asString() != Orchestrated::class.asClassName().simpleName) {
            logger.error("Each property inside an interface annotated with @ViewState should only be annotated with one @Orchestrated.")
            return false
        }

        val triggerType = annotation.arguments[0].value as? KSType
        if (triggerType == null) {
            logger.error("\"trigger\" cannot be null.")
            return false
        }

        val triggerAnnotations = triggerType.declaration.annotations.toList()
        if (triggerAnnotations.size != 1) {
            logger.error("Trigger classes have to be annotated with exactly one @Action.")
            return false
        }

        if (triggerAnnotations.first().shortName.asString() != Action::class.asClassName().simpleName) {
            logger.error("Trigger classes can only be annotated with one @Action.")
            return false
        }

        return true
    }
}
