package com.jeantuffier.generator

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.orchestrate.Feature
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.State
import com.jeantuffier.statemachine.orchestrate.UseCase
import com.jeantuffier.statemachine.processor.generator.UseCaseGenerator
import com.jeantuffier.statemachine.processor.validator.OrchestrationValidator
import com.jeantuffier.statemachine.processor.visitor.FeatureVisitor
import com.jeantuffier.statemachine.processor.visitor.OrchestrationVisitor
import com.jeantuffier.statemachine.processor.visitor.StatePropertyEnumVisitor
import com.jeantuffier.statemachine.processor.visitor.UseCasesVisitor

class OrchestrationProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferredSymbols = mutableListOf<KSAnnotated>()
        generateOrchestration(resolver)
        deferredSymbols.addAll(generateStatePropertyEnums(resolver))
        deferredSymbols.addAll(generateUseCaseEnum(resolver))
        deferredSymbols.addAll(generateFeatures(resolver))

        return deferredSymbols
    }

    /**
     * Checks the validity of each element annotated with [com.jeantuffier.statemachine.orchestrate.Orchestration] and
     * starts generating code.
     */
    private fun generateOrchestration(resolver: Resolver) {
        val items = resolver.getSymbolsWithAnnotation(Orchestration::class.qualifiedName!!).toList()
        val orchestrationValidator = OrchestrationValidator()
        val orchestrationVisitor = OrchestrationVisitor(environment)
        items.filter { orchestrationValidator.isValid(it, environment.logger) }
            .forEach { it.accept(orchestrationVisitor, Unit) }
    }

    private fun generateStatePropertyEnums(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(State::class.qualifiedName!!).toList()
        val statePropertyEnumVisitor = StatePropertyEnumVisitor(environment.codeGenerator)
        val result = symbols.filter { !it.validate() }
        symbols
            .filter { it.validate() }
            .map { it.accept(statePropertyEnumVisitor, Unit) }

        return result
    }

    private fun generateFeatures(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Feature::class.qualifiedName!!).toList()
        val featureVisitor = FeatureVisitor(environment.codeGenerator, environment.logger)
        val result = symbols.filter { !it.validate() }
        symbols
            .filter { it.validate() }
            .map { it.accept(featureVisitor, Unit) }

        return result
    }

    private fun generateUseCaseEnum(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(UseCase::class.qualifiedName!!).toList()
        val visitor = UseCasesVisitor()
        val result = symbols.filter { !it.validate() }
        symbols
            .filter { it.validate() }
            .map { it.accept(visitor, Unit) }
        if (
            !resolver.getNewFiles().toList().map { it.fileName }.contains("UseCases.kt") &&
            symbols.isNotEmpty() &&
            visitor.packageName.isNotBlank() &&
            visitor.useCases.isNotEmpty()
        ) {
            UseCaseGenerator(environment.codeGenerator)
                .generate(visitor.packageName, visitor.useCases, visitor.cancellable)
        }

        return result
    }
}
