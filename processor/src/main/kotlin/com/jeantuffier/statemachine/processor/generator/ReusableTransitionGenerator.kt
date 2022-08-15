package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.jeantuffier.statemachine.Transition
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class ReusableTransitionGenerator(
    private val codeGenerator: CodeGenerator
) {

    fun generateInterface() {
        val fileSpec = FileSpec.builder(
            packageName = "com.jeantuffier.statemachine",
            fileName = "ReusableTransition",
        ).apply {
            addImport(TransitionKeyGenerator.PACKAGE_NAME, TransitionKeyGenerator.ENUM_NAME)
            addImport("com.jeantuffier.statemachine", "Transition")
            addType(
                TypeSpec.funInterfaceBuilder("ReusableTransition")
                    .addTypeVariable(TypeVariableName.Companion.invoke("Key"))
                    .addTypeVariable(TypeVariableName.Companion.invoke("Event"))
                    .addSuperinterface(Transition::class)
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addModifiers(KModifier.ABSTRACT)
                            .addModifiers(KModifier.SUSPEND)
                            .addModifiers(KModifier.OPERATOR)
                            .addParameter("updater", ViewStateUpdaterGenerator.interfaceClassName)
                            .addParameter("event", TypeVariableName.invoke("Event"))
                            .build()
                    )
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}