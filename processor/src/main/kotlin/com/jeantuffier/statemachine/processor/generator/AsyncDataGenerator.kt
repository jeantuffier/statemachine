/*
package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.AsyncDataStatus
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

class AsyncDataGenerator(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) {

    fun generateLoadAsyncDataFunction() {
        val fileSpec = FileSpec.builder(
            packageName = "com.jeantuffier.statemachine",
            fileName = "LoadAsyncData",
        ).apply {
            addImport(AsyncData::class.java.packageName, AsyncData::class.java.simpleName)
            addImport(AsyncDataStatus::class.java.packageName, AsyncDataStatus::class.java.simpleName)
            val loadEvent = TypeVariableName("LoadEvent")
            val asyncDataType = TypeVariableName("AsyncDataType")
            val eitherType = ClassName("arrow.core", "Either")
                .parameterizedBy(TypeVariableName("Error"), asyncDataType)
            val loader = LambdaTypeName.get(
                parameters = arrayOf(loadEvent),
                returnType = eitherType,
            ).copy(suspending = true)

            val reusableTransition = ReusableTransitionGenerator.className.parameterizedBy(loadEvent)
            addFunction(
                FunSpec.builder("loadAsyncData")
                    .addTypeVariable(loadEvent)
                    .addTypeVariable(TypeVariableName("Error"))
                    .addTypeVariable(asyncDataType)
                    .addParameter("key", TransitionKeyGenerator.className)
                    .addParameter("loader", loader)
                    .returns(reusableTransition)
                    .addCode(
                        """
                        |return ReusableTransition { updater, event: LoadEvent -> 
                        |   val currentValue = updater.currentValue<AsyncData<AsyncDataType>>(key)
                        |   updater.updateValue(
                        |       key = key,
                        |       newValue = currentValue.copy(status = AsyncDataStatus.LOADING)
                        |   )
                        |   updater.updateValue(
                        |       key = key,
                        |       newValue = when(val result = loader(event)) {
                        |           is Either.Left -> AsyncData(
                        |               data = null,
                        |               status = AsyncDataStatus.ERROR
                        |           )
                        |           is Either.Right -> AsyncData(
                        |               data = result.value,
                        |               status = AsyncDataStatus.SUCCESS
                        |           )
                        |       }
                        |   )
                        |}
                        |""".trimMargin()
                    )
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    fun generateLoadAsyncDataFlowFunction() {
        val fileSpec = FileSpec.builder(
            packageName = "com.jeantuffier.statemachine",
            fileName = "LoadAsyncDataFlow",
        ).apply {
            addImport(AsyncData::class.java.packageName, AsyncData::class.java.simpleName)
            addImport(AsyncDataStatus::class.java.packageName, AsyncDataStatus::class.java.simpleName)
            val loadEvent = TypeVariableName("LoadEvent")
            val asyncDataType = TypeVariableName("AsyncDataType")
            val eitherType = ClassName("arrow.core", "Either")
                .parameterizedBy(TypeVariableName("Error"), asyncDataType)
            val flowType = ClassName("kotlinx.coroutines.flow", "Flow")
                .parameterizedBy(eitherType)
            val loader = LambdaTypeName.get(
                parameters = arrayOf(loadEvent),
                returnType = flowType,
            ).copy(suspending = true)

            val reusableTransition = ReusableTransitionGenerator.className.parameterizedBy(loadEvent)
            addFunction(
                FunSpec.builder("loadAsyncData")
                    .addTypeVariable(loadEvent)
                    .addTypeVariable(TypeVariableName("Error"))
                    .addTypeVariable(asyncDataType)
                    .addParameter("key", TransitionKeyGenerator.className)
                    .addParameter("loader", loader)
                    .returns(reusableTransition)
                    .addCode(
                        """
                        |return ReusableTransition { updater, event: LoadEvent -> 
                        |   val currentValue = updater.currentValue<AsyncData<AsyncDataType>>(key)
                        |   updater.updateValue(
                        |       key = key,
                        |       newValue = currentValue.copy(status = AsyncDataStatus.LOADING)
                        |   )
                        |   
                        |   loader(event).collect {
                        |       updater.updateValue(
                        |           key = key,
                        |           newValue = when (it) {
                        |               is Either.Left -> AsyncData(
                        |                   data = null,
                        |                   status = AsyncDataStatus.ERROR
                        |               )
                        |               is Either.Right -> AsyncData(
                        |                   data = it.value,
                        |                   status = AsyncDataStatus.SUCCESS
                        |               )
                        |           }
                        |       )
                        |   }
                        |}
                        |""".trimMargin()
                    )
                    .build()
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }
}*/
