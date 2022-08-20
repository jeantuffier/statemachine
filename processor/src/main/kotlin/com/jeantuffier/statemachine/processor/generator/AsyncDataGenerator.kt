package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.jeantuffier.statemachine.Transition
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class AsyncDataGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateLoadDataFunction() {
        val fileSpec = FileSpec.builder(
            packageName = "com.jeantuffier.statemachine",
            fileName = "LoadAsyncData",
        ).apply {
            addImport(ReusableTransitionGenerator.PACKAGE_NAME, ReusableTransitionGenerator.INTERFACE_NAME)
            addImport(TransitionKeyGenerator.PACKAGE_NAME, TransitionKeyGenerator.ENUM_NAME)
            addImport("com.jeantuffier.statemachine", "Transition")
            val loadEvent = TypeVariableName("LoadEvent")
            val asyncDataType = TypeVariableName("AsyncDataType")
            val loader = LambdaTypeName.get(
                parameters = arrayOf(loadEvent),
                returnType = asyncDataType,
            )
            val reusableTransition = ReusableTransitionGenerator.className.parameterizedBy(loadEvent)
            addFunction(
                FunSpec.builder("loadAsyncData")
                    .addTypeVariable(asyncDataType)
                    .addTypeVariable(loadEvent)
                    .addParameter("key", TransitionKeyGenerator.className)
                    .addParameter("loader", loader)
                    .returns(reusableTransition)
                    .addCode(
                        """
                        |return ReusableTransition { updater, event: LoadEvent -> 
                        |val currentValue = updater.currentValue(key) as AsyncData<AsyncDataType>
                        |updater.updateValue(
                        |key = key,
                        |newValue = currentValue.copy(status = AsyncDataStatus.LOADING)
                        |)
                        |val data = loader(event)
                        |updater.updateValue(
                        |key = key,
                        |newValue = AsyncData(
                        |data = data,
                        |status = AsyncDataStatus.SUCCESS
                        |)
                        |)
                        |}
                        |""".trimMargin()
//                        CodeBlock.builder()
//                            .add(
//                                """
//                        |val currentValue = %1L.currentValue(key) as AsyncData<AsyncDataType>
//                        |%1L.updateValue(
//                        |key = key,
//                        |newValue = currentValue.copy(status = AsyncDataStatus.LOADING)
//                        |)
//                        |val data = loader(%2L)
//                        |%1L.updateValue(
//                        |key = key,
//                        |newValue = AsyncData(
//                        |data = data,
//                        |status = AsyncDataStatus.SUCCESS
//                        |)
//                        |)
//                        |""".trimMargin()
//                            )
//                            .build()
                    )
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}