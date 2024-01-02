package com.jeantuffier.statemachine.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.jeantuffier.statemachine.processor.generator.extension.featureImplementationName
import com.jeantuffier.statemachine.processor.generator.extension.hasCancellableUseCases
import com.jeantuffier.statemachine.processor.generator.extension.isSuspendableUseCase
import com.jeantuffier.statemachine.processor.generator.extension.isUseCase
import com.jeantuffier.statemachine.processor.generator.extension.upperCaseSimpleName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class FeatureGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()

        val featureImplementationName = classDeclaration.featureImplementationName()
        val fileName = if (featureImplementationName.isNullOrEmpty()) {
            classDeclaration.toClassName().simpleName + "Store"
        } else featureImplementationName

        val fileSpec = FileSpec.builder(
            packageName = packageName,
            fileName = fileName,
        ).apply {
            addImport("kotlinx.coroutines", "SupervisorJob")
            addImport("kotlinx.coroutines.flow", "asStateFlow")

            val featureClass = TypeSpec.classBuilder(fileName)
                .primaryConstructor(constructorFunSpec())
                .addSuperinterface(classDeclaration.toClassName())
                .addProperty(scopePropertySpec())
                .addProperty(mutableStatePropertySpec(packageName))
                .addProperty(statePropertySpec(packageName))

            if (classDeclaration.hasCancellableUseCases()) {
                featureClass.addProperty(jobPropertySpec(packageName))
            }

            featureClass.addFunctions(implementInterfaceFunctions(classDeclaration))

            addType(featureClass.build())
        }.build()
        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    private fun constructorFunSpec() = FunSpec.constructorBuilder()
        .addParameter("coroutineDispatcher", CoroutineDispatcher::class)
        .build()

    private fun scopePropertySpec() = PropertySpec.builder("scope", CoroutineScope::class)
        .addModifiers(KModifier.PRIVATE)
        .initializer(
            CodeBlock.builder()
                .add("CoroutineScope(SupervisorJob() + coroutineDispatcher)")
                .build()
        )
        .build()

    private fun mutableStatePropertySpec(packageName: String): PropertySpec {
        val mutableStateFlowType =
            ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
                .parameterizedBy(ClassName(packageName, "Counter.CounterState"))
        return PropertySpec.builder("_state", mutableStateFlowType)
            .addModifiers(KModifier.PRIVATE)
            .initializer(
                CodeBlock.builder()
                    .add("MutableStateFlow(Counter.CounterState())")
                    .build()
            )
            .build()
    }

    private fun statePropertySpec(packageName: String): PropertySpec {
        val stateFlowType =
            ClassName("kotlinx.coroutines.flow", "StateFlow")
                .parameterizedBy(ClassName(packageName, "Counter.CounterState"))
        return PropertySpec.builder("state", stateFlowType)
            .initializer(
                CodeBlock.builder()
                    .add("_state.asStateFlow()")
                    .build()
            )
            .build()
    }

    private fun jobPropertySpec(packageName: String): PropertySpec {
        val type = ClassName("kotlin.collections", "MutableMap")
            .parameterizedBy(
                ClassName(packageName, "CancelID"),
                ClassName("kotlinx.coroutines", "Job"),
            )
        return PropertySpec.builder("jobs", type)
            .initializer(
                CodeBlock.builder()
                    .add("mutableMapOf<CancelID, Job>()")
                    .build()
            )
            .build()
    }

    private fun implementInterfaceFunctions(classDeclaration: KSClassDeclaration): List<FunSpec> {
        return classDeclaration.getAllFunctions()
            .filter { !listOf("equals", "hashCode", "toString").contains(it.simpleName.asString())  }
            .map { function ->
                FunSpec.builder(function.simpleName.asString())
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameters(
                        function.parameters.map { parameter ->
                            ParameterSpec.builder(parameter.name!!.asString(), parameter.type.toTypeName())
                                .build()
                        }
                    )
                    .addCode()
                    .build()
            }.toList()
    }

    private fun functionContent(function: KSFunctionDeclaration) {
        if (function.isUseCase())
    }
}