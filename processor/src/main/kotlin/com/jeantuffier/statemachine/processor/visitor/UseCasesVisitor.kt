package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.extension.isSuspending
import com.jeantuffier.statemachine.processor.generator.extension.upperCaseSimpleName

class UseCasesVisitor : KSVisitorVoid() {

    var packageName = ""
    val useCases = mutableSetOf<String>()
    val cancellable = mutableSetOf<String>()

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        if (packageName.isEmpty()) {
            packageName = function.packageName.asString()
        }
        useCases.add(function.upperCaseSimpleName())
        if (function.isSuspending()) {
            cancellable.add(function.upperCaseSimpleName())
        }
    }
}
