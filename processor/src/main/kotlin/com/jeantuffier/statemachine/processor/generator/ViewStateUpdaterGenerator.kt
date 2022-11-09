package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.framework.ViewStateUpdater
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.MutableStateFlow

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
                    .addSuperinterface(viewStateUpdater)
                    .addFunction(currentValue(crossProperties))
                    .addFunction(updateValue(logger, viewStateClass, crossProperties))
                    .addFunction(updateValues())
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    companion object {
        val interfaceClassName = ClassName(PACKAGE_NAME, INTERFACE_NAME)
    }
}

private fun crossProperties(
    viewStateClass: KSClassDeclaration,
): List<String> {
    val annotationType = CrossStateProperty::class

    return viewStateClass.getDeclaredProperties()
        .flatMap { it.annotations }
        .filter { it.checkName(annotationType.qualifiedName) }
        .flatMap { it.arguments }
        .filter { it.name?.asString() == "key" }
        .map { it.value as String }
        .toList()
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
