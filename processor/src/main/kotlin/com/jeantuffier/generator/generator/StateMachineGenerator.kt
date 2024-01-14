/*
package com.jeantuffier.generator.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.core.StateMachine
import com.jeantuffier.statemachine.processor.generator.extension.actionsParameters
import com.jeantuffier.statemachine.processor.generator.extension.findActions
import com.jeantuffier.statemachine.processor.generator.extension.findArgumentValueByName
import com.jeantuffier.statemachine.processor.generator.extension.findOrchestrationAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.hasOrchestratedAnnotation
import com.jeantuffier.statemachine.processor.generator.extension.orchestratedParameters
import com.jeantuffier.statemachine.processor.generator.extension.orchestrationBaseName
import com.jeantuffier.statemachine.processor.generator.extension.packageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.CoroutineDispatcher

*/
/**
 * Generate a function returning an instance of [com.jeantuffier.statemachine.core.StateMachine] for a given class or
 * interface annotated with [com.jeantuffier.statemachine.orchestrate.Orchestration].
 *//*

class StateMachineGenerator(
    private val codeGenerator: CodeGenerator,
) {

    fun generateStateMachine(classDeclaration: KSClassDeclaration) {
        val baseName = classDeclaration.orchestrationBaseName()
        val packageName = classDeclaration.packageName(baseName)
        val stateClass = "${baseName}State"

        val actionClassName = ClassName(packageName, "${baseName}Action")
        val stateClassName = ClassName(packageName, stateClass)
        val returnType = StateMachine::class.asClassName().parameterizedBy(actionClassName, stateClassName)
        val error = classDeclaration.findOrchestrationAnnotation()?.findArgumentValueByName("errorType") ?: return

        val fileSpec = FileSpec.builder(packageName, "${baseName}StateMachine").apply {
            addImport("com.jeantuffier.statemachine.core", "StateMachine")
            addFunction(
                FunSpec.builder(name.replaceFirstChar(Char::lowercaseChar))
                    .addParameters(classDeclaration.orchestratedParameters(error.toClassName()))
                    .addParameters(classDeclaration.actionsParameters(stateClassName))
                    .addParameter(
                        ParameterSpec.builder("initialValue", stateClassName)
                            .build(),
                    )
                    .addParameter(
                        ParameterSpec.builder("coroutineDispatcher", CoroutineDispatcher::class.asClassName())
                            .build(),
                    )
                    .returns(returnType)
                    .addStatement(
                        """
                            return StateMachine(
                                initialValue = initialValue,
                                coroutineDispatcher = coroutineDispatcher,
                                reducer = ${baseName.replaceFirstChar(Char::lowercaseChar)}Reducer(
                                    ${stateMachineReducerParameter(classDeclaration).joinToString(",\n")}
                                )
                            )
                        """.trimIndent(),
                    )
                    .build(),
            )
        }.build()

        fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
    }

    */
/**
     * Generate the list of reducer's parameters to use in the state machine.
     *//*

    private fun stateMachineReducerParameter(classDeclaration: KSClassDeclaration): List<String> {
        val orchestratedProperties = classDeclaration.getAllProperties()
            .filter { it.hasOrchestratedAnnotation() }
            .map { it.simpleName.asString() }
            .toList()
        val actions = classDeclaration.findActions()

        return orchestratedProperties + actions.map {
            it.toClassName().simpleName.replaceFirstChar(Char::lowercaseChar)
        }
    }
}
*/
