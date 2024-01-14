/*
package com.jeantuffier.generator.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.generator.GeneratorProcessor.Companion.PACKAGE_NAME
import com.jeantuffier.generator.generator.extension.upperCaseSimpleName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class StatePropertyEnumGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateStatePropertyEnums(classDeclaration: KSClassDeclaration) {
        val fileName = classDeclaration.toClassName().simpleName + "Property"

        val fileSpec = FileSpec.builder(
            packageName = PACKAGE_NAME,
            fileName = fileName,
        ).apply {
            val enum = TypeSpec.enumBuilder(fileName)
            classDeclaration.getAllProperties()
                .forEach { property -> enum.addEnumConstant(property.upperCaseSimpleName()) }
            addType(enum.build())

            val annotation = TypeSpec.annotationBuilder("Update")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("stateProperty", ClassName(packageName, fileName))
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("stateProperty", ClassName(packageName, fileName))
                        .initializer("stateProperty")
                        .build()
                )
            addType(annotation.build())
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}*/
