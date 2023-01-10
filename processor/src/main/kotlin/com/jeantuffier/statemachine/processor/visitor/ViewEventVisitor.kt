package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ViewStateActionsGenerator

class ViewEventVisitor(
    private val resolver: Resolver,
    private val env: SymbolProcessorEnvironment,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        ViewStateActionsGenerator(env).generateViewEvent(classDeclaration, packageName, resolver)
    }
}
