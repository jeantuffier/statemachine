package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.processor.validator.OrchestrationValidator
import com.jeantuffier.statemachine.processor.visitor.OrchestrationVisitor

class OrchestrationProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unresolvedSymbols: MutableList<KSAnnotated> = mutableListOf()
        unresolvedSymbols.addAll(generateOrchestration(resolver))
        return unresolvedSymbols
    }

    /**
     * Checks the validity of each element annotated with [com.jeantuffier.statemachine.orchestrate.Orchestration] and
     * starts generating code.
     */
    private fun generateOrchestration(resolver: Resolver): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = Orchestration::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver
                .getSymbolsWithAnnotation(annotationName)
                .toList()
            val validatedSymbols = resolved.filter { it.validate() }.toList()

            val orchestrationValidator = OrchestrationValidator()
            val orchestrationVisitor = OrchestrationVisitor(environment)
            validatedSymbols
                .filter {
                    orchestrationValidator.isValid(it, environment.logger)
                }
                .forEach {
                    it.accept(orchestrationVisitor, Unit)
                }

            unresolvedSymbols = resolved - validatedSymbols
        }
        return unresolvedSymbols
    }
}
