package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.SideEffect
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class StateGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateViewState(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val arguments = classDeclaration.annotations.first {
            it.shortName.asString() == Orchestration::class.asClassName().simpleName
        }.arguments
        val baseName = arguments.first().value as String
        val fileName = "${baseName}State"

        val properties = classDeclaration.getAllProperties()

        val fileSpec = FileSpec.builder(packageName, fileName).apply {
            addType(
                TypeSpec.classBuilder(fileName)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(generateViewStateConstructor(properties))
                    .addProperties(generateViewStateProperties(properties))
                    .build(),
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun generateViewStateConstructor(properties: Sequence<KSPropertyDeclaration>): FunSpec {
        val parameterSpecs: MutableList<ParameterSpec> = mutableListOf()
        val specs = properties.mapNotNull { it.generateAsyncDataConstructorParameter() }
        parameterSpecs.addAll(specs)
        parameterSpecs.add(generateSideEffectConstructorParameter())

        return FunSpec.constructorBuilder()
            .addParameters(parameterSpecs)
            .build()
    }

    private fun KSPropertyDeclaration.generateAsyncDataConstructorParameter(): ParameterSpec {
        val propertyName = simpleName.asString()
        val builder = ParameterSpec.builder(propertyName, type.toTypeName())
        if (isData()) {
            builder.defaultValue("%T()", OrchestratedData::class.asTypeName())
        } else {
            builder.defaultValue("%T()", OrchestratedPage::class.asTypeName())
        }
        return builder.build()
    }

    private fun KSPropertyDeclaration.generateConstructorParameter(): ParameterSpec {
        val propertyName = simpleName.asString()
        val propertyType = type.resolve().toClassName()

        return ParameterSpec.builder(propertyName, propertyType).build()
    }

    private fun generateSideEffectConstructorParameter(): ParameterSpec {
        val propertyTypeName = List::class.asTypeName()
            .parameterizedBy(SideEffect::class.asTypeName())
        val emptyList = MemberName("kotlin.collections", "emptyList")
        return ParameterSpec.builder("sideEffects", propertyTypeName)
            .defaultValue("%M()", emptyList)
            .build()
    }

    private fun generateViewStateProperties(properties: Sequence<KSPropertyDeclaration>): List<PropertySpec> {
        val propertySpecs: MutableList<PropertySpec> = mutableListOf()
        val specs = properties.mapNotNull {
            val propertyName = it.simpleName.asString()
            PropertySpec.builder(propertyName, it.type.toTypeName())
                .initializer(propertyName)
                .build()
        }
        propertySpecs.addAll(specs)

        val propertyTypeName = List::class.asTypeName()
            .parameterizedBy(SideEffect::class.asTypeName())
        propertySpecs.add(
            PropertySpec.builder("sideEffects", propertyTypeName)
                .initializer("sideEffects")
                .build(),
        )

        return propertySpecs
    }

    private fun KSPropertyDeclaration.isData(): Boolean =
        type.resolve().toClassName() == OrchestratedData::class.asClassName()
}
