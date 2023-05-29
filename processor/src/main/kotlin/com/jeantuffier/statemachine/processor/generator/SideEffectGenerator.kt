package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
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

    private val generatedSideEffectFiles = mutableListOf<String>()

    fun generateSideEffects(classDeclaration: KSClassDeclaration) {
        val packageName =
            classDeclaration.packageName.asString() + ".sideeffects"
        classDeclaration.getAllProperties()
            .filter { it.isContent() || it.isPagingContent() }
            .forEach {
                val fileName = "${it.simpleName.asString().replaceFirstChar(Char::uppercaseChar)}SideEffects"
                if (generatedSideEffectFiles.contains(fileName)) {
                    logger.warn("$fileName already exists.")
                    return@forEach
                }

                logger.warn("$fileName does not exist.")
                val builder = TypeSpec.classBuilder(fileName)
                    .addModifiers(KModifier.SEALED)
                    .addSuperinterface(SideEffect::class)
                    .addType(
                        TypeSpec.classBuilder("CouldNotBeLoaded")
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .addParameter("id", Long::class)
                                    .addParameter("error", Throwable::class)
                                    .build(),
                            )
                            .addProperty(
                                PropertySpec.builder("id", Long::class)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .initializer("id")
                                    .build(),
                            )
                            .addProperty(
                                PropertySpec.builder("error", Throwable::class)
                                    .initializer("error")
                                    .build(),
                            )
                            .superclass(ClassName(packageName, fileName))
                            .build(),
                    )
                val fileSpec = FileSpec.builder(packageName, fileName).apply {
                    addType(builder.build())
                }.build()
                fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
                generatedSideEffectFiles.add(fileName)
            }
        val sideEffects = classDeclaration.annotations.first().arguments[2].value as List<KSType>
        sideEffects.forEach { type ->
            val fileName = "${type.toClassName().simpleName}SideEffects"
            if (generatedSideEffectFiles.contains(fileName)) {
                logger.warn("$fileName already exists.")
                return@forEach
            }

            logger.warn("$fileName does not exist.")
            val builder = TypeSpec.classBuilder(fileName)
                .addModifiers(KModifier.SEALED)
                .addSuperinterface(SideEffect::class)
                .addType(
                    TypeSpec.classBuilder("Waiting")
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
                    TypeSpec.classBuilder(status)
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
            val fileSpec = FileSpec.builder(packageName, fileName).apply {
                addType(builder.build())
            }.build()
            fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
            generatedSideEffectFiles.add(fileName)
        }
    }

    private fun KSPropertyDeclaration.isContent(): Boolean =
        type.resolve().toClassName() == OrchestratedData::class.asClassName()

    private fun KSPropertyDeclaration.isPagingContent(): Boolean =
        type.resolve().toClassName() == OrchestratedPage::class.asClassName()
}
