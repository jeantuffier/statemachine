package com.jeantuffier.statemachine.processor.generator

import arrow.core.Either
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.UiEvent
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class ViewStateExtensionsGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {
    fun generateImplementation(viewStateClass: KSClassDeclaration, packageName: String) {
        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = "${viewStateClass.simpleName.asString()}Extensions",
        ).apply {
            val crossAsyncProperties = asyncCrossProperties(viewStateClass)
            val crossUiEventProperties = uiEventCrossProperties(logger, viewStateClass)
            addImport(MutableStateFlow::class.java.packageName, MutableStateFlow::class.java.simpleName)
            addImport("kotlinx.coroutines.flow", "update")
            addImport("com.jeantuffier.statemachine.framework", "loadAsyncData")
            crossAsyncProperties.forEach { addFunction(loadFunction(it, viewStateClass.toClassName())) }
            crossAsyncProperties.forEach { addFunction(updateFunction(it, viewStateClass.toClassName())) }
            crossUiEventProperties.forEach {
                addFunction(onUiEvent(it, viewStateClass.toClassName()))
                addFunction(onUiEventHandled(it, viewStateClass.toClassName()))
            }
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}

private fun asyncCrossProperties(viewStateClass: KSClassDeclaration): List<KSPropertyDeclaration> =
    viewStateClass.getDeclaredProperties()
        .filter(::filterAsyncCrossProperties)
        .toList()

private fun filterAsyncCrossProperties(property: KSPropertyDeclaration) =
    property.annotations.any { it.checkName(CrossStateProperty::class.qualifiedName) } &&
            property.type.resolve().toClassName() == AsyncData::class.asClassName()

private fun uiEventCrossProperties(logger: KSPLogger, viewStateClass: KSClassDeclaration): List<KSPropertyDeclaration> =
    viewStateClass.getDeclaredProperties()
        .filter(::filterUiEventCrossProperties)
        .toList()

private fun filterUiEventCrossProperties(property: KSPropertyDeclaration): Boolean {
    val propertyType = property.type.resolve()
    return propertyType.toClassName() == List::class.asClassName() &&
            propertyType.arguments.first().toTypeName() == UiEvent::class.asClassName()
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
    val action = TypeVariableName("Action")
    val error = TypeVariableName("Error")
    val mutableStateFlowType = ClassName(
        MutableStateFlow::class.java.packageName,
        MutableStateFlow::class.java.simpleName,
    ).parameterizedBy(viewStateClass)
    val name = crossProperty.simpleName.asString()
    val type = crossProperty.type.resolve().arguments.first().toTypeName()
    val loadLambda = LambdaTypeName.get(
        parameters = arrayOf(action),
        returnType = ClassName(Either::class.java.packageName, Either::class.java.simpleName)
            .parameterizedBy(error, type)
    ).copy(suspending = true)
    return FunSpec.builder("load${name.capitalize()}")
        .addModifiers(KModifier.SUSPEND)
        .receiver(mutableStateFlowType)
        .addTypeVariable(action)
        .addTypeVariable(error)
        .addParameter(ParameterSpec.builder("action", action).build())
        .addParameter(
            ParameterSpec.builder("loader", loadLambda)
                .build()
        )
        .addStatement(
            """
            | loadAsyncData(value.$name, action, loader)
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

private fun onUiEvent(
    crossProperty: KSPropertyDeclaration,
    viewStateClass: ClassName,
): FunSpec {
    val mutableStateFlowType = ClassName(
        MutableStateFlow::class.java.packageName,
        MutableStateFlow::class.java.simpleName,
    ).parameterizedBy(viewStateClass)
    val name = crossProperty.simpleName.asString()
    val action = TypeVariableName("Action")
    val factoryLambda = LambdaTypeName.get(
        parameters = arrayOf(action),
        returnType = UiEvent::class.asTypeName().copy(nullable = true),
    )
    return FunSpec.builder("on${name.capitalize()}")
        .receiver(mutableStateFlowType)
        .addTypeVariable(action)
        .addParameter(
            ParameterSpec.builder("action", action)
                .build()
        )
        .addParameter(
            ParameterSpec.builder("factory", factoryLambda)
                .build()
        )
        .addStatement("return factory(action)?.let { uiEvent -> update { it.copy(uiEvents = value.uiEvents + uiEvent) } } ?: Unit")
        .build()
}

private fun onUiEventHandled(
    crossProperty: KSPropertyDeclaration,
    viewStateClass: ClassName,
): FunSpec {
    val mutableStateFlowType = ClassName(
        MutableStateFlow::class.java.packageName,
        MutableStateFlow::class.java.simpleName,
    ).parameterizedBy(viewStateClass)
    val name = crossProperty.simpleName.asString()
    return FunSpec.builder("on${name.capitalize()}Handled")
        .receiver(mutableStateFlowType)
        .addParameter(
            ParameterSpec.builder("uiEvent", UiEvent::class.asTypeName())
                .build()
        )
        .addStatement("return update { state -> state.copy(uiEvents = value.uiEvents.filterNot { it.id == uiEvent.id }) }")
        .build()
}

private fun String.capitalize(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
