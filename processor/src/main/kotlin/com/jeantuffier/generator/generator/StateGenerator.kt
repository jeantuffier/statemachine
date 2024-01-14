/*
package com.jeantuffier.generator.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.jeantuffier.statemachine.orchestrate.Event
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.processor.generator.extension.isNotUnit
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedData
import com.jeantuffier.statemachine.processor.generator.extension.isOrchestratedPage
import com.jeantuffier.statemachine.processor.generator.extension.orchestrationBaseName
import com.jeantuffier.statemachine.processor.generator.extension.packageName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

*/
/**
 * Generate the state class corresponding to a class/interface annotated with
 * [com.jeantuffier.statemachine.orchestrate.Orchestration].
 *//*

class StateGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateViewState(classDeclaration: KSClassDeclaration) {
        val baseName = classDeclaration.orchestrationBaseName()
        val packageName = classDeclaration.packageName(baseName)
        val fileName = "${baseName}State"

        val properties = classDeclaration.getAllProperties()

        val fileSpec = FileSpec.builder(packageName, fileName).apply {
            val filteredProperties = properties.filter { it.isNotUnit() }
            addType(
                TypeSpec.classBuilder(fileName)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(generateViewStateConstructor(filteredProperties))
                    .addProperties(generateViewStateProperties(filteredProperties))
                    .build(),
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    */
/**
     * Generate the constructor of the state class.
     *//*

    private fun generateViewStateConstructor(properties: Sequence<KSPropertyDeclaration>): FunSpec {
        val specs = properties.map { generateOrchestratedConstructorParameter(it) }
        val parameterSpecs: MutableList<ParameterSpec> = mutableListOf()
        parameterSpecs.addAll(specs)
        parameterSpecs.add(generateEventConstructorParameter())
        return FunSpec.constructorBuilder()
            .addParameters(parameterSpecs)
            .build()
    }

    */
/**
     * Generate the parameters to use in the constructor of the state class.
     *//*

    private fun generateOrchestratedConstructorParameter(declaration: KSPropertyDeclaration): ParameterSpec {
        val propertyName = declaration.simpleName.asString()
        val builder = ParameterSpec.builder(propertyName, declaration.type.toTypeName())
        val args = when {
            declaration.isOrchestratedData() -> OrchestratedData::class.asTypeName()
            declaration.isOrchestratedPage() -> OrchestratedPage::class.asTypeName()
            else -> null
        }
        if (args != null) {
            builder.defaultValue("%T()", args)
        }
        return builder.build()
    }

    */
/**
     * Generate the event parameter to use in the constructor of the state class.
     *//*

    private fun generateEventConstructorParameter(): ParameterSpec {
        val type = Event::class.asClassName().copy(nullable = true)
        return ParameterSpec.builder("event", type)
            .defaultValue("%L", null)
            .build()
    }

    */
/**
     * Generate the properties to use in the state class.
     *//*

    private fun generateViewStateProperties(properties: Sequence<KSPropertyDeclaration>): List<PropertySpec> {
        val specs = properties.map {
            val propertyName = it.simpleName.asString()
            PropertySpec.builder(propertyName, it.type.toTypeName())
                .initializer(propertyName)
                .build()
        }
        val propertyTypeName = Event::class.asClassName().copy(nullable = true)
        val propertySpecs: MutableList<PropertySpec> = mutableListOf()
        propertySpecs.addAll(specs)
        propertySpecs.add(
            PropertySpec.builder("event", propertyTypeName)
                .initializer("event")
                .build(),
        )

        return propertySpecs
    }
}
*/
