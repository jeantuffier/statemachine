package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ViewStateUpdaterGenerator

class ViewStateVisitor(
    codeGenerator: CodeGenerator
) : KSVisitorVoid() {

    private val viewUpdaterGenerator = ViewStateUpdaterGenerator(codeGenerator)

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) {
        val packageName = classDeclaration.packageName.asString()
        viewUpdaterGenerator.generate(classDeclaration, packageName)
    }
}
