package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

class TransitionKeyVisitor(private val logger: KSPLogger) : KSVisitorVoid() {

    val properties = mutableListOf<String>()

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: Unit
    ) {
        properties.add(key(property))
    }

    private fun key(property: KSPropertyDeclaration) =
        property.annotations
            .flatMap { it.arguments }
            .first { it.name?.asString() == "key" }
            .value as String
}