package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.ViewStateUpdater
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*

class ViewStateUpdaterGenerator(
    private val codeGenerator: CodeGenerator
) {

    fun generate(viewStateClass: KSClassDeclaration, packageName: String) {
        val updaterName = "${viewStateClass.simpleName.asString()}Updater"
        val viewStateTypeName = viewStateClass.asType(emptyList())
            .toTypeName(TypeParameterResolver.EMPTY)

        val mutableStateFlow = ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
        val parameterizedFlow = mutableStateFlow.parameterizedBy(viewStateClass.toClassName())

        val superType = ClassName("com.jeantuffier.statemachine", "ViewStateUpdater")
        val transitionKeyClass = ClassName(TransitionKeyGenerator.PACKAGE_NAME, TransitionKeyGenerator.ENUM_NAME)
        val parameterizedSuperType = superType.parameterizedBy(transitionKeyClass)

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = updaterName,
        ).apply {
            addImport("kotlinx.coroutines.flow", "MutableStateFlow", "update")
            addImport("com.jeantuffier.statemachine", "ViewStateUpdater")
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
                    .superclass(parameterizedSuperType)
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)

    }
}
