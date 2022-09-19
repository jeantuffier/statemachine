package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ViewEventGenerator

class ViewEventVisitor(
    private val logger: KSPLogger,
    private val resolver: Resolver,
    private val viewEventGenerator: ViewEventGenerator,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        viewEventGenerator.generateViewEvent(classDeclaration, packageName, resolver)
    }
}
