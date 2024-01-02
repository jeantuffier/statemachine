package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.StatePropertyEnumGenerator

class StatePropertyEnumVisitor(codeGenerator: CodeGenerator) : KSVisitorVoid() {

    private val statePropertyEnumGenerator = StatePropertyEnumGenerator(codeGenerator)

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        statePropertyEnumGenerator.generateStatePropertyEnums(classDeclaration)
    }
}
