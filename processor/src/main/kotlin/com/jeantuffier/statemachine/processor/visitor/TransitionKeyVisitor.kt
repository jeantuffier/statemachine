package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.camelToUpperSnakeCase

class TransitionKeyVisitor : KSVisitorVoid() {

    val properties = mutableListOf<String>()

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) {
        properties.addAll(declaredProperties(classDeclaration))
    }

    private fun declaredProperties(viewStateClass: KSClassDeclaration) =
        viewStateClass.getDeclaredProperties()
            .map {
                it.simpleName.asString()
                    .camelToUpperSnakeCase()
            }.toList()
}