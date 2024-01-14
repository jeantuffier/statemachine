package com.jeantuffier.generator.generator.extension

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlinx.coroutines.flow.Flow

fun KSFunctionDeclaration.upperCaseSimpleName() = simpleName.asString().replaceFirstChar(Char::uppercase)

fun KSFunctionDeclaration.isSuspending() = modifiers.contains(Modifier.SUSPEND)

fun KSFunctionDeclaration.returnsFlow() =
    returnType?.resolve()?.toClassName() == Flow::class.asClassName()

fun KSFunctionDeclaration.propertyToUpdate(): String {
    val enumValue = annotations.first { it.isUpdate() }
        .arguments
        .first().value as KSType
    return enumValue.lowerCaseSimpleName()
}
