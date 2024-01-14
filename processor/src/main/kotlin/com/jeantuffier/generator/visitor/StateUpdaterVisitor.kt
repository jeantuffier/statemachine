package com.jeantuffier.generator.visitor

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.generator.generator.extension.upperCaseSimpleName

class StateUpdaterVisitor : KSVisitorVoid() {
    val stateUpdaters = mutableSetOf<String>()

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        stateUpdaters.add(function.upperCaseSimpleName())
    }
}
