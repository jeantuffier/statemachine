package com.jeantuffier.generator

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.jeantuffier.generator.generator.StateUpdateGenerator
import com.jeantuffier.generator.visitor.FeatureVisitor
import com.jeantuffier.generator.visitor.StateUpdaterVisitor
import com.jeantuffier.statemachine.orchestrate.Feature
import com.jeantuffier.statemachine.orchestrate.StateUpdater

class GeneratorProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.logger.warn("Starting process")
        val deferredSymbols = mutableListOf<KSAnnotated>()
        // deferredSymbols.addAll(generateStatePropertyEnums(resolver))
        deferredSymbols.addAll(generateStateUpdaterEnum(resolver))
        deferredSymbols.addAll(generateFeatures(resolver))
        environment.logger.warn("Process finished")

        return deferredSymbols
    }

    /**
     * Checks the validity of each element annotated with [com.jeantuffier.statemachine.orchestrate.Orchestration] and
     * starts generating code.
     */
    /*private fun generateOrchestration(resolver: Resolver) {
        val items = resolver.getSymbolsWithAnnotation(Orchestration::class.qualifiedName!!).toList()
        val orchestrationValidator = OrchestrationValidator()
        val orchestrationVisitor = OrchestrationVisitor(environment)
        items.filter { orchestrationValidator.isValid(it, environment.logger) }
            .forEach { it.accept(orchestrationVisitor, Unit) }
    }*/

    /*private fun generateStatePropertyEnums(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(State::class.qualifiedName!!).toList()
        val statePropertyEnumVisitor = StatePropertyEnumVisitor(environment.codeGenerator)
        val result = symbols.filter { !it.validate() }
        symbols
            .filter { it.validate() }
            .map { it.accept(statePropertyEnumVisitor, Unit) }

        return result
    }*/

    private fun generateStateUpdaterEnum(resolver: Resolver): List<KSAnnotated> {
        environment.logger.warn("Starting generateStateUpdaterEnum")
        val symbols = resolver.getSymbolsWithAnnotation(StateUpdater::class.qualifiedName!!).toList()
        val visitor = StateUpdaterVisitor()
        val result = symbols.filter { !it.validate() }
        symbols
            .filter { it.validate() }
            .map { it.accept(visitor, Unit) }
        if (
            !resolver.getNewFiles().toList().map { it.fileName }.contains("StateUpdate.kt") &&
            symbols.isNotEmpty() &&
            visitor.stateUpdaters.isNotEmpty()
        ) {
            StateUpdateGenerator(environment.codeGenerator)
                .generate(visitor.stateUpdaters)
        }

        return result
    }

    private fun generateFeatures(resolver: Resolver): List<KSAnnotated> {
        environment.logger.warn("Starting generateFeatures")
        val featureSymbols = resolver.getSymbolsWithAnnotation(Feature::class.qualifiedName!!).toList()

        environment.logger.warn("featureSymbols found: $featureSymbols")
        if (featureSymbols.isEmpty()) {
            return emptyList()
        }

        val notValidatedFeatureSymbols = featureSymbols.filter { !it.validate() }
        if (notValidatedFeatureSymbols.isNotEmpty()) {
            environment.logger.warn("not validated symbols: $notValidatedFeatureSymbols")
            return notValidatedFeatureSymbols
        }

        environment.logger.warn("All symbols are validated before feature generation")

        val featureVisitor = FeatureVisitor(environment.codeGenerator, environment.logger, resolver)
        featureSymbols.map { it.accept(featureVisitor, Unit) }

        return emptyList()
    }

    companion object {
        const val PACKAGE_NAME = "com.jeantuffier.generator"
    }
}
