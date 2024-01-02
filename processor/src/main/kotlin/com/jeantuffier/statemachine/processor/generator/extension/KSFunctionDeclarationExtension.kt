package com.jeantuffier.statemachine.processor.generator.extension

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

fun KSFunctionDeclaration.upperCaseSimpleName() = simpleName.asString().replaceFirstChar(Char::uppercase)

fun KSFunctionDeclaration.isSuspending() = modifiers.contains(Modifier.SUSPEND)

fun KSFunctionDeclaration.isAssociatedWithAUseCase() = annotations.any {
    it.isUseCase()
}

fun KSFunctionDeclaration.isSuspendableUseCase() = annotations.any {
    it.isUseCase()
} && isSuspending()