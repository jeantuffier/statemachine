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
        /*generateKeys(resolver, logger, codeGenerator)
        generateViewUpdaterInterface(resolver, logger, codeGenerator)
        generateReusableTransition(resolver, logger, codeGenerator)
        generateLoadAsyncData(resolver, logger, codeGenerator)*/
        generateViewUpdaters(resolver, environment)

        return emptyList()
    }
}

private fun Resolver.symbolsWithAnnotations(annotationName: String) =
    getSymbolsWithAnnotation(annotationName)
        .toList()

/*private fun generateKeys(
    resolver: Resolver,
    logger: KSPLogger,
    codeGenerator: CodeGenerator,
) {
    val annotationName = CrossStateProperty::class.qualifiedName
    annotationName ?: return

    val propertyAnnotations = resolver.symbolsWithAnnotations(annotationName)
    val validator = CrossStatePropertyValidator(logger)
    val propertySymbols = propertyAnnotations.filter { validator.isValid(it) }
    val transitionKeyVisitor = TransitionKeyVisitor(logger)
    val transitionKeyGenerator = TransitionKeyGenerator(logger, codeGenerator)

    propertySymbols
        .forEachIndexed { index, validSymbol ->
            validSymbol.accept(transitionKeyVisitor, Unit)
            if (index == propertySymbols.lastIndex) {
                transitionKeyGenerator.generateTransitionKeys(transitionKeyVisitor.properties)
            }
        }
}*/

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

/*private fun generateViewUpdaterInterface(
    resolver: Resolver,
    logger: KSPLogger,
    codeGenerator: CodeGenerator,
) {
    if (!resolver.checkFileExists("ViewStateUpdater.kt")) {
        ViewStateUpdaterGenerator(logger, codeGenerator)
            .generateInterface()
    }
}*/

/*private fun generateReusableTransition(
    resolver: Resolver,
    logger: KSPLogger,
    codeGenerator: CodeGenerator,
) {
    if (!resolver.checkFileExists("ReusableTransition.kt")) {
        ReusableTransitionGenerator(codeGenerator)
            .generateInterface()
    }
}*/

/*private fun generateLoadAsyncData(
    resolver: Resolver,
    logger: KSPLogger,
    codeGenerator: CodeGenerator,
) {
    if (!resolver.checkFileExists("LoadAsyncData.kt")) {
        with(AsyncDataGenerator(logger, codeGenerator)) {
            generateLoadAsyncDataFunction()
            generateLoadAsyncDataFlowFunction()
        }
    }
}*/

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
