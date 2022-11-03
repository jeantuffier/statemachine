package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
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
            addImport("com.jeantuffier.statemachine.framework", "AsyncData")
            addImport("com.jeantuffier.statemachine.framework", "AsyncDataStatus")
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
                        |val currentValue = updater.currentValue<AsyncData<AsyncDataType>>(key)
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
                    )
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}