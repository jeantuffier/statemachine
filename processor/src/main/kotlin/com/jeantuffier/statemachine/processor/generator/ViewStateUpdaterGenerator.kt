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
import com.jeantuffier.statemachine.framework.ViewStateUpdater
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

private const val PACKAGE_NAME = "com.jeantuffier.statemachine"
private const val INTERFACE_NAME = "ViewStateUpdater"

class ViewStateUpdaterGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {
    fun generateImplementation(viewStateClass: KSClassDeclaration, packageName: String) {
        val updaterName = "${viewStateClass.simpleName.asString()}Updater"

        val parameterizedFlow = ClassName(
            MutableStateFlow::class.java.packageName,
            MutableStateFlow::class.java.simpleName,
        ).parameterizedBy(viewStateClass.toClassName())

        val transitionKey = ClassName(
            TransitionKeyGenerator.PACKAGE_NAME,
            TransitionKeyGenerator.ENUM_NAME,
        )

        val viewStateUpdater = ClassName(
            ViewStateUpdater::class.java.packageName,
            ViewStateUpdater::class.java.simpleName,
        ).parameterizedBy(transitionKey)

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = updaterName,
        ).apply {
            val crossProperties = crossProperties(viewStateClass)
            addImport(MutableStateFlow::class.java.packageName, MutableStateFlow::class.java.simpleName)
            addImport("kotlinx.coroutines.flow", "update")
            addImport("com.jeantuffier.statemachine.framework", "loadAsyncData")
            addType(
                TypeSpec.classBuilder(updaterName)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("mutableStateFlow", parameterizedFlow)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("mutableStateFlow", parameterizedFlow)
                            .initializer("mutableStateFlow")
                            .addModifiers(KModifier.PRIVATE)
                            .build()
                    )
                    //.addSuperinterface(viewStateUpdater)
                    .addProperties(properties(crossProperties))
                    .addFunctions(loadFunctions(crossProperties))
                    .addFunctions(updateFunctions(crossProperties))
                    //.addFunction(currentValue(crossProperties))
                    //.addFunction(updateValue(logger, viewStateClass, crossProperties))
                    //.addFunction(updateValues())
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    companion object {
        val interfaceClassName = ClassName(PACKAGE_NAME, INTERFACE_NAME)
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


    /*return viewStateClass.getDeclaredProperties()
        .flatMap { it.annotations }
        .filter {
            it.it.checkName(annotationType.qualifiedName)
        }
        .flatMap {

            it.arguments
        }
        .filter { it.name?.asString() == "key" }
        .map { it.value as String }
        .toList()*/
}

private fun KSAnnotation.checkName(name: String?): Boolean =
    annotationType
        .resolve()
        .declaration
        .qualifiedName
        ?.asString() == name

private fun currentValue(crossProperties: List<String>): FunSpec {
    val builder = FunSpec.builder("currentValue")
        .addModifiers(KModifier.OVERRIDE)
        .addTypeVariable(TypeVariableName("T"))
        .addParameter("key", TransitionKeyGenerator.className)
        .returns(TypeVariableName("T"))
        .beginControlFlow("return when (key)")

    crossProperties.forEach {
        builder.addStatement("TransitionKey.$it -> mutableStateFlow.value.$it as T")
    }

    return builder
        .addStatement("else -> throw Exception(\"Key not supported\")")
        .endControlFlow()
        .build()
}

private fun properties(crossProperties: List<KSPropertyDeclaration>): List<PropertySpec> {
    return crossProperties.map {
        val name = it.simpleName.asString()
        val type = it.type.toTypeName()
        PropertySpec.builder(name, type)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return mutableStateFlow.value.$name")
                    .build()
            )
            .build()
    }
}

private fun loadFunctions(crossProperties: List<KSPropertyDeclaration>): List<FunSpec> {
    val event = TypeVariableName("Event")
    val error = TypeVariableName("Error")
    return crossProperties.map {
        val name = it.simpleName.asString()
        val type = it.type.resolve().arguments.first().toTypeName()
        val loadLambda = LambdaTypeName.get(
            parameters = arrayOf(event),
            returnType = ClassName(Either::class.java.packageName, Either::class.java.simpleName)
                .parameterizedBy(error, type)
        ).copy(suspending = true)
        FunSpec.builder("load${name.capitalize()}")
            .addModifiers(KModifier.SUSPEND)
            .addTypeVariable(event)
            .addTypeVariable(error)
            .addParameter(ParameterSpec.builder("event", event).build())
            .addParameter(
                ParameterSpec.builder("loader", loadLambda)
                    .build())
            .addStatement(
                """
                | loadAsyncData($name, event, loader)
                |     .collect(::update${name.capitalize()})
                """.trimMargin()
            )
            .build()
    }
}

private fun updateFunctions(crossProperties: List<KSPropertyDeclaration>): List<FunSpec> {
    return crossProperties.map {
        val name = it.simpleName.asString()
        val type = it.type.toTypeName()
        FunSpec.builder("update${name.capitalize()}")
            .addParameter(ParameterSpec.builder("newValue", type).build())
            .addStatement(
                """
                | mutableStateFlow.update { it.copy($name = newValue) }
                """.trimMargin()
            )
            .build()
    }
}

private fun String.capitalize(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

private fun ViewStateUpdaterGenerator.updateValue(
    logger: KSPLogger,
    viewStateClass: KSClassDeclaration,
    crossProperties: List<String>,
): FunSpec {
    val builder = FunSpec.builder("updateValue")
        .addModifiers(KModifier.OVERRIDE)
        .addTypeVariable(TypeVariableName("T"))
        .addParameter("key", TransitionKeyGenerator.className)
        .addParameter("newValue", TypeVariableName("T"))
        .beginControlFlow("when (key)")

    crossProperties.forEach { propertyName ->
        val property = viewStateClass.getDeclaredProperties()
            .toList()
            .firstOrNull { it.simpleName.asString() == propertyName }
        if (property != null) {
            val type = property.type.resolve().toTypeName()
            builder.addStatement(
                "TransitionKey.$propertyName -> mutableStateFlow.update { it.copy($propertyName = newValue as $type) }"
            )
        }
    }

    return builder
        .addStatement("else -> {}")
        .endControlFlow()
        .build()
}

private fun ViewStateUpdaterGenerator.updateValues(): FunSpec {
    val valuesType = ClassName("kotlin.collections", "Map")
    val transitionKeyClass = TransitionKeyGenerator.className
    val parameterizedValues = valuesType.parameterizedBy(transitionKeyClass, TypeVariableName("T"))
    return FunSpec.builder("updateValues")
        .addModifiers(KModifier.OVERRIDE)
        .addTypeVariable(TypeVariableName("T"))
        .addParameter("values", parameterizedValues)
        .addStatement("values.entries.forEach { updateValue(it.key, it.value) }")
        .build()
}
