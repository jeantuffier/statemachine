package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class ViewEventGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateViewEvent(
        builderClass: KSClassDeclaration,
        packageName: String,
        resolver: Resolver,
    ) {
        val viewEventsClassName = builderClass.simpleName.asString().replace("Builder", "")
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
    ): TypeSpec {
        val constructorParameters = event.getAllProperties().toList()
        val name = event.simpleName.asString().replace("Interface", "")
        val isCrossEvent = event.annotations.firstOrNull()?.shortName?.asString() == "CrossViewEvent"
        val builder = typeSpecBuilder(event, name)
            .superclass(superType)

        if (event.classKind != ClassKind.OBJECT) {
            builder.primaryConstructor(constructor(constructorParameters))
                .addProperties(properties(constructorParameters, isCrossEvent))
        }

        if (isCrossEvent) {
            builder.addSuperinterface(ClassName(event.packageName.asString(), event.simpleName.asString()))
        }

        return builder.build()
    }

    private fun typeSpecBuilder(event: KSClassDeclaration, name: String): TypeSpec.Builder =
        if (event.classKind == ClassKind.OBJECT) {
            TypeSpec.objectBuilder(name)
        } else {
            TypeSpec.classBuilder(name)
        }

    private fun KSClassDeclaration.eventsToAdd(resolver: Resolver): List<KSClassDeclaration> {
        val crossViewEventArguments = annotations.first().arguments.first().value as List<KSType>
        return crossViewEventArguments.map { it.declaration.qualifiedName }
            .mapNotNull { ksName ->
                ksName?.let { resolver.getClassDeclarationByName(it) }
            }.toList()
    }

    private fun constructor(constructorParameters: List<KSPropertyDeclaration>) = FunSpec.constructorBuilder().apply {
        constructorParameters.forEach { constructorParameter ->
            addParameter(
                name = constructorParameter.toString(),
                type = constructorParameter.type.toTypeName(),
            )
        }
    }.build()

    private fun properties(
        constructorParameters: List<KSPropertyDeclaration>,
        isCrossEvent: Boolean
    ): List<PropertySpec> =
        constructorParameters.map { constructorParameter ->
            val name = constructorParameter.toString()
            PropertySpec.builder(
                name = name,
                type = constructorParameter.type.toTypeName(),
                modifiers = if (isCrossEvent) listOf(KModifier.OVERRIDE) else emptyList(),
            )
                .initializer(name)
                .build()
        }
}
