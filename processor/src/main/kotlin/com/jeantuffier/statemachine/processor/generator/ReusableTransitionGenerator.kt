package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.jeantuffier.statemachine.framework.Transition
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo

class ReusableTransitionGenerator(
    private val codeGenerator: CodeGenerator
) {

    fun generateInterface() {
        val fileSpec = FileSpec.builder(
            packageName = PACKAGE_NAME,
            fileName = INTERFACE_NAME,
        ).apply {
            addImport(TransitionKeyGenerator.PACKAGE_NAME, TransitionKeyGenerator.ENUM_NAME)
            addImport("com.jeantuffier.statemachine.framework", "Transition")
            addType(
                TypeSpec.funInterfaceBuilder("ReusableTransition")
                    .addTypeVariable(TypeVariableName("Event"))
                    .addSuperinterface(Transition::class)
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addModifiers(KModifier.ABSTRACT)
                            .addModifiers(KModifier.SUSPEND)
                            .addModifiers(KModifier.OPERATOR)
                            .addParameter("updater", ViewStateUpdaterGenerator.interfaceClassName)
                            .addParameter("event", TypeVariableName("Event"))
                            .build()
                    )
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    companion object {
        const val PACKAGE_NAME = "com.jeantuffier.statemachine"
        const val INTERFACE_NAME = "ReusableTransition"
        val className = ClassName(PACKAGE_NAME, INTERFACE_NAME)
    }
}