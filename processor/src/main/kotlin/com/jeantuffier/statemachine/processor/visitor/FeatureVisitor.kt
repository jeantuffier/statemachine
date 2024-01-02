package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.FeatureGenerator

class FeatureVisitor(codeGenerator: CodeGenerator, logger: KSPLogger) : KSVisitorVoid() {

    private val featureGenerator = FeatureGenerator(codeGenerator, logger)

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        featureGenerator.generate(classDeclaration)
    }
}
