package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ViewStateUpdaterGenerator

class ViewStateVisitor(
    private val viewUpdaterGenerator: ViewStateUpdaterGenerator
) : KSVisitorVoid() {

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) {
        val packageName = classDeclaration.packageName.asString()
        viewUpdaterGenerator.generateImplementation(classDeclaration, packageName)
    }
}
