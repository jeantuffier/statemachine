package com.jeantuffier.statemachine.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.processor.generator.TransitionKeyGenerator
import com.jeantuffier.statemachine.processor.validator.SymbolValidator
import com.jeantuffier.statemachine.processor.visitor.TransitionKeyVisitor
import com.jeantuffier.statemachine.processor.visitor.ViewStateVisitor

class ViewStateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {


    private var keysPackageName = ""

    private val validator = SymbolValidator(logger)
    private val transitionKeyVisitor = TransitionKeyVisitor()
    private val viewStateVisitor = ViewStateVisitor(codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var unresolvedSymbols: List<KSAnnotated> = emptyList()
        val annotationName = ViewState::class.qualifiedName

        if (annotationName != null) {
            val resolved = resolver.symbolsWithAnnotations(annotationName)
            val validatedSymbols = resolved.validatedSymbols(validator)

            val transitionKeyClass = resolver.getNewFiles()
                .toList()
                .firstOrNull { it.fileName == "TransitionKey.kt" }

            logger.info("TransitionKey exists: ${transitionKeyClass != null}")

            unresolvedSymbols = if (transitionKeyClass == null) {
                logger.info("Generating TransitionKey")
                generateKeys(validatedSymbols, transitionKeyVisitor, codeGenerator)
                resolved
            } else {
                logger.info("Generating ViewStateUpdaters")
                validatedSymbols
                    .forEach { it.accept(viewStateVisitor, Unit) }
                resolved - validatedSymbols.toSet()
            }
        }
        return unresolvedSymbols
    }
}

private fun Resolver.symbolsWithAnnotations(annotationName: String) =
    getSymbolsWithAnnotation(annotationName)
        .toList()

private fun List<KSAnnotated>.validatedSymbols(validator: SymbolValidator) =
    filter { it.validate() }
        .filter { validator.isValid(it) }

private fun generateKeys(
    validatedSymbols: List<KSAnnotated>,
    transitionKeyVisitor: TransitionKeyVisitor,
    codeGenerator: CodeGenerator,
) {
    validatedSymbols
        .forEachIndexed { index, validSymbol ->
            validSymbol.accept(transitionKeyVisitor, Unit)
            if (index == validatedSymbols.lastIndex) {
                TransitionKeyGenerator(codeGenerator).generateTransitionKeys(transitionKeyVisitor.properties)
            }
        }
}