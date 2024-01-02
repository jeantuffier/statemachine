package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class UseCaseGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(
        packageName: String,
        useCases: Set<String>,
        cancellable: Set<String>,
    ) {
        val fileName = "UseCases"

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = fileName,
        ).apply {
            val useCaseEnum = TypeSpec.enumBuilder(fileName)
            useCases.forEach { useCaseEnum.addEnumConstant(it) }
            addType(useCaseEnum.build())

            val cancellableEnum = TypeSpec.enumBuilder("CancelID")
            cancellable.forEach { cancellableEnum.addEnumConstant(it) }
            addType(cancellableEnum.build())

            val cancellableInterface = TypeSpec.interfaceBuilder("CancellableUseCase")
                .addFunction(
                    FunSpec.builder("cancel")
                        .addParameter("id", ClassName(packageName, "CancelID"))
                        .addModifiers(KModifier.ABSTRACT)
                        .build()
                )
                .build()
            addType(cancellableInterface)

            val annotation = TypeSpec.annotationBuilder("With")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("useCase", ClassName(packageName, fileName))
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("useCase", ClassName(packageName, fileName))
                        .initializer("useCase")
                        .build()
                )
            addType(annotation.build())
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}
