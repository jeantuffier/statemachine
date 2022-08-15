package com.jeantuffier.statemachine.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate

class CrossStatePropertyValidator(private val logger: KSPLogger) {

    fun isValid(symbol: KSAnnotated) =
        symbol is KSPropertyDeclaration
                && symbol.validate()
}
