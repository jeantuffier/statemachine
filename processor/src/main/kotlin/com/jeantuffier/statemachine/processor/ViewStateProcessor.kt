package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.processor.generator.ReusableTransitionGenerator
import com.jeantuffier.statemachine.processor.generator.TransitionKeyGenerator
import com.jeantuffier.statemachine.processor.generator.ViewStateUpdaterGenerator
import com.jeantuffier.statemachine.processor.validator.CrossStatePropertyValidator
import com.jeantuffier.statemachine.processor.validator.ViewStateValidator
import com.jeantuffier.statemachine.processor.visitor.TransitionKeyVisitor
import com.jeantuffier.statemachine.processor.visitor.ViewStateVisitor

class ViewStateProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val viewStateValidator = ViewStateValidator(logger)
    private val crossStatePropertyValidator = CrossStatePropertyValidator(logger)

    private val transitionKeyGenerator = TransitionKeyGenerator(logger, codeGenerator)
    private val transitionKeyVisitor = TransitionKeyVisitor(logger)

    private val viewUpdaterGenerator = ViewStateUpdaterGenerator(logger, codeGenerator)
    private val viewStateVisitor = ViewStateVisitor(viewUpdaterGenerator)

    private val reusableTransitionGenerator = ReusableTransitionGenerator(codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val crossStatePropertyAnnotationName = CrossStateProperty::class.qualifiedName
        val viewStateAnnotationName = ViewState::class.qualifiedName

        if (viewStateAnnotationName != null && crossStatePropertyAnnotationName != null) {
            val resolvedCrossStatePropertyAnnotations =
                resolver.symbolsWithAnnotations(crossStatePropertyAnnotationName)
            val resolvedViewStateAnnotations = resolver.symbolsWithAnnotations(viewStateAnnotationName)

            val validatedCrossStatePropertySymbols =
                resolvedCrossStatePropertyAnnotations.filter { crossStatePropertyValidator.isValid(it) }
            val validatedViewStateSymbols = resolvedViewStateAnnotations.filter { viewStateValidator.isValid(it) }

            unresolvedSymbols = if (!resolver.checkFileExists("TransitionKey.kt")) {
                logger.info("Generating TransitionKey")
                generateKeys(logger, validatedCrossStatePropertySymbols, transitionKeyVisitor, transitionKeyGenerator)
                resolvedViewStateAnnotations
            } else if (!resolver.checkFileExists("ViewStateUpdater.kt")) {
                logger.info("Generating ViewStateUpdater")
                viewUpdaterGenerator.generateInterface()
                resolvedViewStateAnnotations
            } else if (!resolver.checkFileExists("ReusableTransition.kt")) {
                logger.info("Generating ReusableTransition")
                reusableTransitionGenerator.generateInterface()
                resolvedViewStateAnnotations
            } else {
                logger.info("Generating ViewStateUpdaters")
                resolvedViewStateAnnotations
                    .forEach { it.accept(viewStateVisitor, Unit) }
                resolvedViewStateAnnotations - validatedViewStateSymbols.toSet()
            }
        }
        return unresolvedSymbols
    }
}

private fun Resolver.symbolsWithAnnotations(annotationName: String) =
    getSymbolsWithAnnotation(annotationName)
        .toList()

private fun Resolver.checkFileExists(name: String) =
    getAllFiles()
        .toList()
        .firstOrNull { it.fileName == name } != null

private fun generateKeys(
    logger: KSPLogger,
    validatedSymbols: List<KSAnnotated>,
    transitionKeyVisitor: TransitionKeyVisitor,
    transitionKeyGenerator: TransitionKeyGenerator,
) {
    val names = validatedSymbols.joinToString(",") { it.toString() }
    logger.info("Validated symbols for TransitionKey: $names")
    validatedSymbols
        .forEachIndexed { index, validSymbol ->
            validSymbol.accept(transitionKeyVisitor, Unit)
            if (index == validatedSymbols.lastIndex) {
                transitionKeyGenerator.generateTransitionKeys(transitionKeyVisitor.properties)
            }
        }
}