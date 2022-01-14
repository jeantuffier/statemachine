package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.Content
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.PagingContent
import com.jeantuffier.statemachine.orchestrate.SideEffect
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

class ActionsGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator
) {

    fun generateActions(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val baseName = classDeclaration.annotations.first {
            it.shortName.asString() == Orchestration::class.asClassName().simpleName
        }.arguments.first().value as String
        val fileName = "${baseName}Action"

        val eventsToAdd = classDeclaration.eventsToAdd()

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = fileName,
        ).apply {
            val sealedClass = TypeSpec.classBuilder(fileName)
                .addModifiers(KModifier.SEALED)

            val className = ClassName("", fileName)
            eventsToAdd.forEach { event ->
                val typeSpec = addSealedElement(event, className)
                sealedClass.addType(typeSpec)
            }
            if (classDeclaration.sideEffects().isNotEmpty()) {
                val constructorSpec = FunSpec.constructorBuilder()
                    .addParameter("sideEffect", SideEffect::class.asClassName())
                    .build()
                val propertySpec = PropertySpec.builder("sideEffect", SideEffect::class.asClassName())
                    .initializer("sideEffect")
                    .build()
                val typeSpec = TypeSpec.classBuilder("SideEffectHandled")
                    .primaryConstructor(constructorSpec)
                    .addProperty(propertySpec)
                    .superclass(className)
                    .build()
                sealedClass.addType(typeSpec)
            }
            addType(sealedClass.build())
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun KSClassDeclaration.eventsToAdd(): Set<KSClassDeclaration> {
        val orchestratedEvents = getAllProperties()
            .filter { isContent(it) || isPagingContent(it) }
            .mapNotNull { property ->
                val type = property.annotations.first().arguments[0].value as KSType
                type.declaration.closestClassDeclaration()
            }
            .toSet()
        val sideEffectsEvent = sideEffects()
            .mapNotNull { it.declaration.closestClassDeclaration() }
            .toSet()
        return orchestratedEvents + sideEffectsEvent
    }

    private fun isContent(property: KSPropertyDeclaration): Boolean =
        property.type.resolve().toClassName() == Content::class.asClassName()

    private fun isPagingContent(property: KSPropertyDeclaration): Boolean =
        property.type.resolve().toClassName() == PagingContent::class.asClassName()

    private fun addSealedElement(
        action: KSClassDeclaration,
        superType: TypeName,
    ): TypeSpec {
        val constructorParameters = action.getAllProperties().toList()
        val isAction = action.annotations.firstOrNull()?.shortName?.asString() == Action::class.java.simpleName

        val builder = if (action.getAllProperties().toList().isEmpty()) {
            TypeSpec.objectBuilder(action.simpleName.asString())
        } else {
            TypeSpec.classBuilder(action.simpleName.asString())
                .primaryConstructor(constructor(constructorParameters))
                .addProperties(properties(constructorParameters, isAction))
        }
        builder.superclass(superType)

        val typeVariables = action.typeParameters.map { it.toTypeVariableName() }
        val baseSuperInterface = ClassName(action.packageName.asString(), action.simpleName.asString())
        val superInterface = if (typeVariables.isNotEmpty()) {
            typeVariables.forEach { builder.addTypeVariable(it) }
            baseSuperInterface.parameterizedBy(typeVariables)
        } else baseSuperInterface
        builder.addSuperinterface(superInterface)

        return builder.build()
    }

    private fun constructor(constructorParameters: List<KSPropertyDeclaration>) =
        FunSpec.constructorBuilder().apply {
            constructorParameters.forEach { constructorParameter ->
                val type = if (constructorParameter.type.resolve().declaration is KSTypeParameter) {
                    ((constructorParameter.type.resolve().declaration) as KSTypeParameter).toTypeVariableName()
                } else {
                    constructorParameter.type.toTypeName()
                }
                addParameter(
                    name = constructorParameter.toString(),
                    type = type,
                )
            }
        }.build()

    private fun properties(
        constructorParameters: List<KSPropertyDeclaration>,
        isAction: Boolean
    ): List<PropertySpec> =
        constructorParameters.map { constructorParameter ->
            val name = constructorParameter.toString()
            PropertySpec.builder(
                name = name,
                type = if (constructorParameter.type.resolve().declaration is KSTypeParameter) {
                    ((constructorParameter.type.resolve().declaration) as KSTypeParameter).toTypeVariableName()
                } else {
                    constructorParameter.type.toTypeName()
                },
                modifiers = if (isAction) listOf(KModifier.OVERRIDE) else emptyList(),
            )
                .initializer(name)
                .build()
        }

    private fun KSClassDeclaration.sideEffects() = annotations.first().arguments[2].value as List<KSType>
}
