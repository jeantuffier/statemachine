package com.jeantuffier.statemachine.processor.generator

import arrow.core.Either
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class ViewStateUpdaterGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {
    fun generateImplementation(viewStateClass: KSClassDeclaration, packageName: String) {
        val fileName = "${viewStateClass.simpleName.asString()}Extensions"

        val parameterizedFlow = ClassName(
            MutableStateFlow::class.java.packageName,
            MutableStateFlow::class.java.simpleName,
        ).parameterizedBy(viewStateClass.toClassName())

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = fileName,
        ).apply {
            val crossProperties = crossProperties(viewStateClass)
            addImport(MutableStateFlow::class.java.packageName, MutableStateFlow::class.java.simpleName)
            addImport("kotlinx.coroutines.flow", "update")
            addImport("com.jeantuffier.statemachine.framework", "loadAsyncData")
            crossProperties.forEach { addFunction(loadFunction(it, viewStateClass.toClassName())) }
            crossProperties.forEach { addFunction(updateFunction(it, viewStateClass.toClassName())) }
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}

private fun crossProperties(viewStateClass: KSClassDeclaration): List<KSPropertyDeclaration> {
    val annotationType = CrossStateProperty::class

    return viewStateClass.getDeclaredProperties()
        .filter { property ->
            property.annotations.any {
                it.checkName(annotationType.qualifiedName)
            }
        }.toList()
}

private fun KSAnnotation.checkName(name: String?): Boolean =
    annotationType
        .resolve()
        .declaration
        .qualifiedName
        ?.asString() == name

private fun loadFunction(
    crossProperty: KSPropertyDeclaration,
    viewStateClass: ClassName,
): FunSpec {
    val event = TypeVariableName("Event")
    val error = TypeVariableName("Error")
    val mutableStateFlowType = ClassName(
        MutableStateFlow::class.java.packageName,
        MutableStateFlow::class.java.simpleName,
    ).parameterizedBy(viewStateClass)
    val name = crossProperty.simpleName.asString()
    val type = crossProperty.type.resolve().arguments.first().toTypeName()
    val loadLambda = LambdaTypeName.get(
        parameters = arrayOf(event),
        returnType = ClassName(Either::class.java.packageName, Either::class.java.simpleName)
            .parameterizedBy(error, type)
    ).copy(suspending = true)
    return FunSpec.builder("load${name.capitalize()}")
        .addModifiers(KModifier.SUSPEND)
        .receiver(mutableStateFlowType)
        .addTypeVariable(event)
        .addTypeVariable(error)
        .addParameter(ParameterSpec.builder("event", event).build())
        .addParameter(
            ParameterSpec.builder("loader", loadLambda)
                .build()
        )
        .addStatement(
            """
            | loadAsyncData(value.$name, event, loader)
            |     .collect(::update${name.capitalize()})
        """.trimMargin()
        )
        .build()
}

private fun updateFunction(
    crossProperty: KSPropertyDeclaration,
    viewStateClass: ClassName,
): FunSpec {
    val name = crossProperty.simpleName.asString()
    val type = crossProperty.type.toTypeName()
    val receiver = ClassName(MutableStateFlow::class.java.packageName, MutableStateFlow::class.java.simpleName)
        .parameterizedBy(viewStateClass)
    return FunSpec.builder("update${name.capitalize()}")
        .receiver(receiver)
        .addParameter(ParameterSpec.builder("newValue", type).build())
        .addStatement("return update { it.copy($name = newValue) }")
        .build()
}

private fun String.capitalize(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
