package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ViewEventGenerator

class ViewEventVisitor(
    private val resolver: Resolver,
    private val env: SymbolProcessorEnvironment,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
//        val platforms = env.platforms.map { it }
//        val options = env.options.map { "${it.key}:${it.value}" }.joinToString()
//        env.logger.warn("ViewEventVisitor | options: $options")
        val packageName = classDeclaration.packageName.asString()
        ViewEventGenerator(env).generateViewEvent(classDeclaration, packageName, resolver)
    }
}
