package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class TransitionKeyGenerator(
    private val codeGenerator: CodeGenerator
) {

    fun generateTransitionKeys(properties: List<String>) {
        val setValues = properties.toSet()

        val fileSpec = FileSpec.builder(
            packageName = PACKAGE_NAME,
            fileName = ENUM_NAME,
        ).apply {
            addType(
                TypeSpec.enumBuilder(ENUM_NAME).apply {
                    setValues.forEach {
                        addEnumConstant(it)
                    }
                }.build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    companion object {
        const val PACKAGE_NAME = "com.jeantuffier.statemachine"
        const val ENUM_NAME = "TransitionKey"
    }
}

fun String.camelToUpperSnakeCase(): String {
    val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    return camelRegex
        .replace(this) { "_${it.value}" }
        .uppercase()
}
