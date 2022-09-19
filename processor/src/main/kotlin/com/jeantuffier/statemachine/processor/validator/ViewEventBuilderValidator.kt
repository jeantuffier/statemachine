package com.jeantuffier.statemachine.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate

class ViewEventBuilderValidator(
    private val logger: KSPLogger,
) {

    fun isValid(symbol: KSAnnotated): Boolean =
        symbol is KSClassDeclaration
                && symbol.validate()
                && symbol.checkCrossViewEvents()

    private fun KSClassDeclaration.checkCrossViewEvents(): Boolean {
        val crossViewEventArguments = annotations.first()
            .arguments
            .first()
            .value as List<KSType>

        val annotations = crossViewEventArguments
            .flatMap { it.declaration.annotations }
            .toList()

        return annotations.all {
            it.annotationType
                .resolve()
                .declaration.simpleName.asString() == "CrossViewEvent"
        }
    }
}