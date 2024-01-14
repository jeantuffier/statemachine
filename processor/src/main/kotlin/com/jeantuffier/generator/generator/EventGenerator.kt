/*
package com.jeantuffier.generator.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.orchestrate.Event
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedData
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedPage
import com.jeantuffier.statemachine.processor.generator.extension.upperCaseSimpleName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

*/
/**
 * Generate the events representing the failure of functions loading data in properties annotated with
 * [com.jeantuffier.statemachine.orchestrate.Orchestrated].
 *//*

class EventGenerator(
    private val codeGenerator: CodeGenerator,
) {

    private val generatedEventFiles = mutableListOf<String>()

    fun generateEvents(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString() + ".events"
        classDeclaration.getAllProperties()
            .filter { it.isOrchestratedData() || it.isOrchestratedPage() }
            .forEach {
                val fileName = "CouldNotLoad${it.upperCaseSimpleName()}"
                if (generatedEventFiles.contains(fileName)) {
                    return@forEach
                }

                val builder = TypeSpec.classBuilder(fileName)
                    .addModifiers(KModifier.DATA)
                    .addSuperinterface(Event::class)
                    .primaryConstructor(
                        FunSpec.constructorBuilder().apply {
                            addParameter(
                                name = "error",
                                type = Throwable::class,
                            )
                        }.build(),
                    )
                    .addProperty(
                        PropertySpec.builder("error", Throwable::class)
                            .initializer("error")
                            .build(),
                    )
                val fileSpec = FileSpec.builder(packageName, fileName).apply {
                    addType(builder.build())
                }.build()
                fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
                generatedEventFiles.add(fileName)
            }
    }
}
*/
