package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.processor.validator.OrchestrationValidator
import com.jeantuffier.statemachine.processor.visitor.OrchestrationVisitor

class OrchestrationProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private val orchestrationValidator = OrchestrationValidator()
    private val orchestrationVisitor = OrchestrationVisitor(environment)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unresolvedSymbols: MutableList<KSAnnotated> = mutableListOf()

        unresolvedSymbols.addAll(generateOrchestration(resolver, environment, orchestrationValidator, orchestrationVisitor))

        return unresolvedSymbols
    }
}

private fun Resolver.symbolsWithAnnotations(annotationName: String) =
    getSymbolsWithAnnotation(annotationName)
        .toList()

private fun generateOrchestration(
    resolver: Resolver,
    environment: SymbolProcessorEnvironment,
    orchestrationValidator: OrchestrationValidator,
    orchestrationVisitor: OrchestrationVisitor,
): List<KSAnnotated> {
    var unresolvedSymbols: List<KSAnnotated> = emptyList()
    val annotationName = Orchestration::class.qualifiedName

    if (annotationName != null) {
        val resolved = resolver
            .getSymbolsWithAnnotation(annotationName)
            .toList()
        val validatedSymbols = resolved.filter { it.validate() }.toList()

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

private fun generateViewActions(
    resolver: Resolver,
    env: SymbolProcessorEnvironment,
) {
    /*val annotationName = ViewActionsBuilder::class.qualifiedName
    annotationName ?: return

    val annotations = resolver.symbolsWithAnnotations(annotationName)
    val symbols = annotations.filter { ViewActionsBuilderValidator(env.logger).isValid(it) }

    val exists = symbols.all { viewEventFileExists(it, resolver, env.logger) }
    if (!exists) {
        val viewEventVisitor = ViewEventVisitor(resolver, env)
        annotations.forEach { it.accept(viewEventVisitor, Unit) }
    }*/
}

private fun viewEventFileExists(
    annotated: KSAnnotated,
    resolver: Resolver,
    logger: KSPLogger,
): Boolean {
    val name = (annotated as KSClassDeclaration).simpleName
    return resolver.checkFileExists("${name}ViewEvent.kt")
}

private fun generateViewUpdaters(
    resolver: Resolver,
    environment: SymbolProcessorEnvironment,
) {
    /*val annotationName = ViewState::class.qualifiedName
    annotationName ?: return

    val annotations = resolver.symbolsWithAnnotations(annotationName)
    val validator = ViewStateValidator()
    val visitor = ViewStateVisitor(
        ViewStateExtensionsGenerator(
            environment.logger,
            environment.codeGenerator,
            resolver,
        )
    )

    val symbols = annotations.filter { validator.isValid(it, environment.logger) }
    symbols.forEach { it.accept(visitor, Unit) }*/
}

private fun Resolver.checkFileExists(name: String) =
    getAllFiles()
        .toList()
        .firstOrNull { it.fileName == name } != null
