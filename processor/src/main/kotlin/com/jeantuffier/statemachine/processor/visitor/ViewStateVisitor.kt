package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ViewStateExtensionsGenerator

class ViewStateVisitor(
    private val viewUpdaterGenerator: ViewStateExtensionsGenerator
) : KSVisitorVoid() {

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) {
        val packageName = classDeclaration.packageName.asString()
        viewUpdaterGenerator.generateImplementation(classDeclaration, packageName)
    }
}
