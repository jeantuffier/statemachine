package com.jeantuffier.generator.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.generator.generator.FeatureGenerator

class FeatureVisitor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val resolver: Resolver
) : KSVisitorVoid() {

    private val featureGenerator = FeatureGenerator(codeGenerator, logger)

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        featureGenerator.generate(classDeclaration, resolver)
    }
}
