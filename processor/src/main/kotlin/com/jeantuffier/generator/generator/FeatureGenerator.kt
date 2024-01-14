package com.jeantuffier.generator.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.jeantuffier.generator.GeneratorProcessor.Companion.PACKAGE_NAME
import com.jeantuffier.generator.generator.extension.featureImplementationName
import com.jeantuffier.generator.generator.extension.flowStateUpdater
import com.jeantuffier.generator.generator.extension.hasSuspendableStateUpdater
import com.jeantuffier.generator.generator.extension.isAssociatedWithSuspendingStateUpdater
import com.jeantuffier.generator.generator.extension.stateUpdaterId
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

class FeatureGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    fun generate(classDeclaration: KSClassDeclaration, resolver: Resolver) {
        val featureImplementationName = classDeclaration.featureImplementationName()
        val fileName = if (featureImplementationName.isNullOrEmpty()) {
            classDeclaration.toClassName().simpleName + "Store"
        } else featureImplementationName

        val fileSpec = FileSpec.builder(
            packageName = PACKAGE_NAME,
            fileName = fileName,
        ).apply {
            addImport("kotlinx.coroutines", "SupervisorJob", "launch")
            addImport("kotlinx.coroutines.flow", "asStateFlow", "update")

            val featureClass = TypeSpec.classBuilder(fileName)
                .primaryConstructor(constructorFunSpec())
                .addSuperinterface(classDeclaration.toClassName())
                .addProperty(scopePropertySpec())
                .addProperty(mutableStatePropertySpec(packageName))
                .addProperty(statePropertySpec(packageName))

            logger.warn("Checking for suspendable use cases")
            if (classDeclaration.hasSuspendableStateUpdater(resolver, classDeclaration.packageName.asString())) {
                featureClass.addProperty(jobPropertySpec(packageName))
            }

            featureClass.addFunctions(implementInterfaceFunctions(classDeclaration, resolver))

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
            .addModifiers(KModifier.OVERRIDE)
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
                ClassName(packageName, "StateUpdaterID"),
                ClassName("kotlinx.coroutines", "Job"),
            )
        return PropertySpec.builder("jobs", type)
            .initializer(
                CodeBlock.builder()
                    .add("mutableMapOf()")
                    .build()
            )
            .build()
    }

    private fun implementInterfaceFunctions(
        classDeclaration: KSClassDeclaration,
        resolver: Resolver,
    ): List<FunSpec> {
        return classDeclaration.getAllFunctions()
            .filter { !listOf("equals", "hashCode", "toString").contains(it.simpleName.asString()) }
            .map { function ->
                FunSpec.builder(function.simpleName.asString())
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameters(
                        function.parameters.map { parameter ->
                            ParameterSpec.builder(parameter.name!!.asString(), parameter.type.toTypeName())
                                .build()
                        }
                    )
                    .addCode(functionContent(function, resolver, classDeclaration.packageName.asString()))
                    .build()
            }.toList()
    }

    private fun functionContent(
        function: KSFunctionDeclaration,
        resolver: Resolver,
        packageName: String,
    ): CodeBlock {
        val stateUpdaterId = function.stateUpdaterId() ?: return CodeBlock.of("")
        val coreContent = coreContent(function, resolver, packageName, stateUpdaterId)
        val content = when {
            function.simpleName.asString() == "cancel" -> "jobs[id]?.cancel()"
            function.isAssociatedWithSuspendingStateUpdater(resolver, packageName) ->
                suspendFunWrapper(stateUpdaterId, coreContent)
            else -> coreContent
        }
        return CodeBlock.of(content)
    }

    private fun coreContent(
        function: KSFunctionDeclaration,
        resolver: Resolver,
        packageName: String,
        stateUpdaterId: String,
    ): String {
        return if (function.flowStateUpdater(resolver, packageName)) {
            """
            $stateUpdaterId().collect { newState ->
                _state.update { newState }
            }  
            """.trimIndent()
        } else {
            "_state.update { $stateUpdaterId() }"
        }
    }

    private fun suspendFunWrapper(stateUpdaterId: String, content: String) = """
        jobs[StateUpdaterID.${stateUpdaterId.replaceFirstChar(Char::uppercase)}]?.cancel()
        scope.launch { 
            $content 
        }
        .also { jobs[StateUpdaterID.${stateUpdaterId.replaceFirstChar(Char::uppercase)}] = it }
    """.trimIndent()
}