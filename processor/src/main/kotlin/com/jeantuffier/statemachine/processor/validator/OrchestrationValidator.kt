package com.jeantuffier.statemachine.processor.validator

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.Pagination
import com.jeantuffier.statemachine.processor.generator.extension.findArgumentValueByName
import com.jeantuffier.statemachine.processor.generator.extension.findOrchestratedAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.isAction
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedPage
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName

class OrchestrationValidator {

    fun isValid(symbol: KSAnnotated, logger: KSPLogger): Boolean {
        return symbol is KSClassDeclaration &&
            symbol.validate() &&
            symbol.validateClassNames(logger) &&
            symbol.validateProperties(logger)
    }

    /**
     * Checks that the baseName property value of [com.jeantuffier.statemachine.orchestrate.Orchestration] is not
     * empty.
     */
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

    /**
     * Checks that a class/interface annotated with [com.jeantuffier.statemachine.orchestrate.Orchestration] contains
     * at least one property annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated].
     */
    private fun KSClassDeclaration.validateProperties(logger: KSPLogger): Boolean {
        val properties = getAllProperties()
        if (properties.toList().isEmpty()) {
            logger.error("An interface annotated with @Orchestration should contains at least one property.")
            return false
        }
        properties.forEach { property ->
            validateOrchestrationProperty(property, logger)
        }

        return true
    }

    /**
     * Checks the following for each class/interface annotated with [com.jeantuffier.statemachine.orchestrate.Orchestration]
     * - Each property annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated] must have an action class.
     * - An action class can only be annotated with [com.jeantuffier.statemachine.orchestrate.Action], nothing else.
     * - The action for a property of type [com.jeantuffier.statemachine.orchestrate.OrchestratedPage] must
     * implements [com.jeantuffier.statemachine.orchestrate.Pagination].
     */
    private fun validateOrchestrationProperty(
        property: KSPropertyDeclaration,
        logger: KSPLogger,
    ): Boolean {
        val propertyAnnotations = property.annotations.toList()
        if (propertyAnnotations.isEmpty()) return true

        val orchestratedAnnotation = property.findOrchestratedAnnotation() ?: return true
        val actionType = orchestratedAnnotation.findArgumentValueByName("action")
            ?.declaration
            ?.closestClassDeclaration()
        if (actionType == null) {
            logger.error("Action for $property cannot be null.")
            return false
        }

        val actionAnnotations = actionType.annotations.toList()
        if (actionAnnotations.size != 1) {
            logger.error("Action classes have to be annotated with exactly one @Action.")
            return false
        }

        if (!actionAnnotations.first().isAction()) {
            logger.error("Action classes can only be annotated with one @Action. ($property)")
            return false
        }

        val isOrchestratedPage = property.type.resolve().isOrchestratedPage()
        val implementsPagination = actionType
            .superTypes
            .mapNotNull { it.resolve().declaration.closestClassDeclaration()?.toClassName() }
            .contains(Pagination::class.asClassName())

        if (isOrchestratedPage && !implementsPagination) {
            logger.error("The action $actionType class for $property has to implement PageLoader.")
            return false
        }

        return true
    }
}
