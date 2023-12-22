package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.jeantuffier.statemachine.processor.generator.extension.findActions
import com.jeantuffier.statemachine.processor.generator.extension.findActionDeclaration
import com.jeantuffier.statemachine.processor.generator.extension.hasActionAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.orchestrationBaseName
import com.jeantuffier.statemachine.processor.generator.extension.packageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Generates the sealed class containing actions for a [com.jeantuffier.statemachine.orchestrate.Orchestration]
 * annotation.
 *
 * To make KotlinPoet create data classes for each action, we need to build classes with matching constructor
 * parameters and class properties. [addSealedElement], [constructor] and [properties] are in charge of that.
 */
class ActionsGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateActions(classDeclaration: KSClassDeclaration) {
        val baseName = classDeclaration.orchestrationBaseName()
        val packageName = classDeclaration.packageName(baseName)
        val fileName = "${baseName}Action"

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = fileName,
        ).apply {
            val sealedClass = TypeSpec.classBuilder(fileName)
                .addModifiers(KModifier.SEALED)

            val className = ClassName("", fileName)
            classDeclaration.actionsToAdd().forEach { action ->
                val typeSpec = addSealedElement(action, className)
                sealedClass.addType(typeSpec)
            }

            sealedClass.addType(
                TypeSpec.objectBuilder("EventHandled")
                    .superclass(className)
                    .build(),
            )

            addType(sealedClass.build())
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    /**
     * Create a set of actions to add to the sealed class.
     */
    private fun KSClassDeclaration.actionsToAdd(): Set<KSClassDeclaration> {
        val orchestrated = getAllProperties()
            .filter { it.annotations.toList().isNotEmpty() }
            .mapNotNull { it.findActionDeclaration() }
            .toSet()
        val actions = findActions()
            .mapNotNull { it.declaration.closestClassDeclaration() }
            .toSet()
        return orchestrated + actions
    }

    /**
     * Create a [TypeSpec] instance representing the actions to add in the sealed class.
     */
    private fun addSealedElement(
        classDeclaration: KSClassDeclaration,
        superType: TypeName,
    ): TypeSpec {
        val constructorParameters = classDeclaration.getAllProperties().toList()
        val builder = if (classDeclaration.getAllProperties().toList().isEmpty()) {
            TypeSpec.objectBuilder(classDeclaration.simpleName.asString())
        } else {
            TypeSpec.classBuilder(classDeclaration.simpleName.asString())
                .primaryConstructor(constructor(constructorParameters))
                .addProperties(properties(constructorParameters, classDeclaration.hasActionAnnotation()))
        }
        builder.superclass(superType)

        val typeVariables = classDeclaration.typeParameters.map { it.toTypeVariableName() }
        val baseSuperInterface = classDeclaration.toClassName()
        val superInterface = if (typeVariables.isNotEmpty()) {
            typeVariables.forEach { builder.addTypeVariable(it) }
            baseSuperInterface.parameterizedBy(typeVariables)
        } else {
            baseSuperInterface
        }
        builder.addSuperinterface(superInterface)

        return builder.build()
    }

    /**
     * Create a [FunSpec] instance representing the constructor for an action.
     */
    private fun constructor(constructorParameters: List<KSPropertyDeclaration>) =
        FunSpec.constructorBuilder().apply {
            constructorParameters.forEach { constructorParameter ->
                val declaration = constructorParameter.type.resolve().declaration
                val type = if (declaration is KSTypeParameter) {
                    declaration.toTypeVariableName()
                } else {
                    constructorParameter.type.toTypeName()
                }
                addParameter(
                    name = constructorParameter.toString(),
                    type = type,
                )
            }
        }.build()

    /**
     * Create a list of [PropertySpec] for an action in the sealed class.
     */
    private fun properties(
        constructorParameters: List<KSPropertyDeclaration>,
        isAction: Boolean,
    ): List<PropertySpec> =
        constructorParameters.map { constructorParameter ->
            val name = constructorParameter.toString()
            val declaration = constructorParameter.type.resolve().declaration
            PropertySpec.builder(
                name = name,
                type = if (declaration is KSTypeParameter) {
                    declaration.toTypeVariableName()
                } else {
                    constructorParameter.type.toTypeName()
                },
                modifiers = if (isAction) listOf(KModifier.OVERRIDE) else emptyList(),
            )
                .initializer(name)
                .build()
        }
}
