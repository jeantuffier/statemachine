package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.statemachine.orchestrate.Content
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.PagingContent
import com.jeantuffier.statemachine.orchestrate.SideEffect
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class SideEffectGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateSideEffects(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val arguments = classDeclaration.annotations.first {
            it.shortName.asString() == Orchestration::class.asClassName().simpleName
        }.arguments
        val baseName = arguments.first().value as String
        val fileName = "${baseName}SideEffects"

        val builder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(SideEffect::class)

        classDeclaration.getAllProperties()
            .filter { it.isContent() || it.isPagingContent() }
            .forEach {
                builder.addType(
                    TypeSpec.classBuilder("CouldNotLoad${it.upperCaseSimpleName()}")
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("id", Long::class)
                                .build(),
                        )
                        .addProperty(
                            PropertySpec.builder("id", Long::class)
                                .addModifiers(KModifier.OVERRIDE)
                                .initializer("id")
                                .build(),
                        )
                        .superclass(ClassName(packageName, fileName))
                        .build(),
                )
            }
        val sideEffects = classDeclaration.annotations.first().arguments[2].value as List<KSType>
        sideEffects.forEach { type ->
            builder.addType(
                TypeSpec.classBuilder("WaitingFor${type.declaration.simpleName.asString()}")
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("id", Long::class)
                            .build(),
                    )
                    .addProperty(
                        PropertySpec.builder("id", Long::class)
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("id")
                            .build(),
                    )
                    .superclass(ClassName(packageName, fileName))
                    .build(),
            )
            listOf("Succeeded", "Failed").forEach { status ->
                builder.addType(
                    TypeSpec.classBuilder("${type.declaration.simpleName.asString()}$status")
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("id", Long::class)
                                .build(),
                        )
                        .addProperty(
                            PropertySpec.builder("id", Long::class)
                                .addModifiers(KModifier.OVERRIDE)
                                .initializer("id")
                                .build(),
                        )
                        .superclass(ClassName(packageName, fileName))
                        .build(),
                )
            }
        }

        val fileSpec = FileSpec.builder(packageName, fileName).apply {
            addType(builder.build())
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun KSPropertyDeclaration.isContent(): Boolean =
        type.resolve().toClassName() == Content::class.asClassName()

    private fun KSPropertyDeclaration.isPagingContent(): Boolean =
        type.resolve().toClassName() == PagingContent::class.asClassName()

    private fun KSPropertyDeclaration.upperCaseSimpleName(): String =
        simpleName.asString().replaceFirstChar(Char::uppercaseChar)
}
