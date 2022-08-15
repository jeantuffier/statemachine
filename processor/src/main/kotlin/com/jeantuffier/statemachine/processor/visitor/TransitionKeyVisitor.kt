package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.annotation.CrossStateProperty

class TransitionKeyVisitor(private val logger: KSPLogger) : KSVisitorVoid() {

    val properties = mutableListOf<String>()

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) {
        properties.addAll(declaredProperties(classDeclaration))
    }

    private fun declaredProperties(
        viewStateClass: KSClassDeclaration,
    ): List<String> {
        val annotationType = CrossStateProperty::class
        return viewStateClass.getDeclaredProperties()
            .flatMap { it.annotations }
            .filter { it.checkName(annotationType.qualifiedName) }
            .flatMap { it.arguments }
            .filter { it.name?.asString() == "key" }
            .map { it.value as String }
            .toList()
    }

    private fun KSAnnotation.checkName(name: String?): Boolean =
        annotationType
            .resolve()
            .declaration
            .qualifiedName
            ?.asString() == name
}