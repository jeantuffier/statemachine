package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class TransitionKeyGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateTransitionKeys(properties: List<String>) {
        val fileSpec = FileSpec.builder(
            packageName = PACKAGE_NAME,
            fileName = ENUM_NAME,
        ).apply {
            addType(enum(properties))
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun enum(properties: List<String>) =
        TypeSpec.enumBuilder(ENUM_NAME).apply {
            properties.forEach {
                addEnumConstant(it)
            }
        }.build()

    companion object {
        const val PACKAGE_NAME = "com.jeantuffier.statemachine"
        const val ENUM_NAME = "TransitionKey"

        val className = ClassName(PACKAGE_NAME, ENUM_NAME)
    }
}
