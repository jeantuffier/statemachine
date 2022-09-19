package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

class ViewEventGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateViewEvent(
        builderClass: KSClassDeclaration,
        packageName: String,
        resolver: Resolver,
    ) {
        val viewEventsClassName = builderClass.simpleName.asString().replace("ViewEventsBuilder", "ViewEvents")
        val eventsToAdd = builderClass.eventsToAdd(resolver)

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = viewEventsClassName,
        ).apply {
            val sealedClass = TypeSpec.classBuilder(viewEventsClassName)
                .addModifiers(KModifier.SEALED)

            val className = ClassName("", viewEventsClassName)
            eventsToAdd.forEach { event ->
                val typeSpec = addSealedElement(event, className)
                sealedClass.addType(typeSpec)
            }

            addType(sealedClass.build())


        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun addSealedElement(
        event: KSClassDeclaration,
        superType: TypeName,
    ) : TypeSpec {
        val constructorParameters = constructorParameters(event)
        val builder = topLevelBuilder(event, event.simpleName.asString())
        if (event.classKind != ClassKind.OBJECT) {
            builder.primaryConstructor(constructor(constructorParameters))
                .addProperties(properties(constructorParameters))
        }
        return builder
            .superclass(superType)
            .build()
    }

    private fun KSClassDeclaration.eventsToAdd(resolver: Resolver): List<KSClassDeclaration> {
        val crossViewEventArguments = annotations.first().arguments.first().value as List<KSType>
        return crossViewEventArguments.map { it.declaration.qualifiedName }
            .mapNotNull { ksName ->
                ksName?.let { resolver.getClassDeclarationByName(it) }
            }.toList()
    }

    private fun topLevelBuilder(event: KSClassDeclaration, name: String) = when (event.classKind) {
        ClassKind.CLASS -> TypeSpec.classBuilder(name)
        ClassKind.INTERFACE -> TypeSpec.interfaceBuilder(name)
        ClassKind.OBJECT -> TypeSpec.objectBuilder(name)
        else -> throw Error()
    }

    private fun constructorParameters(event: KSClassDeclaration): List<KSValueParameter> =
        event.primaryConstructor?.parameters ?: emptyList()

    private fun constructor(constructorParameters: List<KSValueParameter>) = FunSpec.constructorBuilder().apply {
        constructorParameters.forEach { constructorParameter ->
            addParameter(
                name = constructorParameter.name?.asString() ?: "", type = constructorParameter.type.toTypeName()
            )
        }
    }.build()

    private fun properties(constructorParameters: List<KSValueParameter>): List<PropertySpec> =
        constructorParameters.mapNotNull { constructorParameter ->
            if (constructorParameter.isVal) {
                val name = constructorParameter.name?.asString() ?: ""
                PropertySpec.builder(
                    name = name,
                    type = constructorParameter.type.toTypeName(),
                )
                    .initializer(name)
                    .build()
            } else null
        }
}
