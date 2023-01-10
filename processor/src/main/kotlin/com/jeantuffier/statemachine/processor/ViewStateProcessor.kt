package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jeantuffier.statemachine.annotation.ViewActionsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.processor.generator.ViewStateUpdaterGenerator
import com.jeantuffier.statemachine.processor.validator.ViewActionsBuilderValidator
import com.jeantuffier.statemachine.processor.validator.ViewStateValidator
import com.jeantuffier.statemachine.processor.visitor.ViewEventVisitor
import com.jeantuffier.statemachine.processor.visitor.ViewStateVisitor

class ViewStateProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        generateViewEvents(resolver, environment)
        generateViewUpdaters(resolver, environment)

        return emptyList()
    }
}

private fun Resolver.symbolsWithAnnotations(annotationName: String) =
    getSymbolsWithAnnotation(annotationName)
        .toList()

private fun generateViewEvents(
    resolver: Resolver,
    env: SymbolProcessorEnvironment,
) {
    val annotationName = ViewActionsBuilder::class.qualifiedName
    annotationName ?: return

    val annotations = resolver.symbolsWithAnnotations(annotationName)
    val symbols = annotations.filter { ViewActionsBuilderValidator(env.logger).isValid(it) }

    val exists = symbols.all { viewEventFileExists(it, resolver, env.logger) }
    if (!exists) {
        val viewEventVisitor = ViewEventVisitor(resolver, env)
        annotations.forEach { it.accept(viewEventVisitor, Unit) }
    }
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
    val annotationName = ViewState::class.qualifiedName
    annotationName ?: return

    val annotations = resolver.symbolsWithAnnotations(annotationName)
    val validator = ViewStateValidator(environment.logger)
    val visitor = ViewStateVisitor(ViewStateUpdaterGenerator(environment.logger, environment.codeGenerator))

    val symbols = annotations.filter { validator.isValid(it) }
    symbols.forEach { it.accept(visitor, Unit) }
}

private fun Resolver.checkFileExists(name: String) =
    getAllFiles()
        .toList()
        .firstOrNull { it.fileName == name } != null
