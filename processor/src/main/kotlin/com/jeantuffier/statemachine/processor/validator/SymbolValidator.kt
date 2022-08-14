package com.jeantuffier.statemachine.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

class SymbolValidator(private val logger: KSPLogger) {

    fun isValid(symbol: KSAnnotated): Boolean {
        return symbol is KSClassDeclaration //1
                && symbol.validate()
                && symbol.isDataClass(logger)
    }
}

private fun KSClassDeclaration.isDataClass(logger: KSPLogger): Boolean {
    val isDataclass = modifiers.contains(Modifier.DATA) &&
            classKind == ClassKind.CLASS
    if (!isDataclass) {
        logger.error("Classes annotated with @ViewState must be data classes")
    }
    return isDataclass
}
