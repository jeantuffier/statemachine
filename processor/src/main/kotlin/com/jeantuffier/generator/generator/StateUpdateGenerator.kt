package com.jeantuffier.generator.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.jeantuffier.generator.GeneratorProcessor.Companion.PACKAGE_NAME
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class StateUpdateGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(ids: Set<String>) {
        val fileSpec = FileSpec.builder(
            packageName = PACKAGE_NAME,
            fileName = "StateUpdate",
        ).apply {
            val stateUpdaterIdEnum = TypeSpec.enumBuilder("StateUpdaterID")
            ids.forEach {
                stateUpdaterIdEnum.addEnumConstant(it.replaceFirstChar(Char::uppercaseChar))
            }
            addType(stateUpdaterIdEnum.build())

            val cancellableInterface = TypeSpec.interfaceBuilder("CancellableStateUpdater")
                .addFunction(
                    FunSpec.builder("cancel")
                        .addParameter("id", ClassName(packageName, "StateUpdaterID"))
                        .addModifiers(KModifier.ABSTRACT)
                        .build()
                )
                .build()
            addType(cancellableInterface)

            val annotation = TypeSpec.annotationBuilder("With")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("stateUpdater", ClassName(packageName, "StateUpdaterID"))
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("stateUpdater", ClassName(packageName, "StateUpdaterID"))
                        .initializer("stateUpdater")
                        .build()
                )
            addType(annotation.build())
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}
