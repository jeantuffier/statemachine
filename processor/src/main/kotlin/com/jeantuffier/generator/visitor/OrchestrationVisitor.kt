/*
package com.jeantuffier.generator.visitor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ActionsGenerator
import com.jeantuffier.statemachine.processor.generator.EventGenerator
import com.jeantuffier.statemachine.processor.generator.HelpersGenerator
import com.jeantuffier.statemachine.processor.generator.ReducerGenerator
import com.jeantuffier.statemachine.processor.generator.StateGenerator
import com.jeantuffier.statemachine.processor.generator.StateMachineGenerator

class OrchestrationVisitor(environment: SymbolProcessorEnvironment) : KSVisitorVoid() {

    private val viewStateGenerator = StateGenerator(environment.codeGenerator)
    private val viewActionsGenerator = ActionsGenerator(environment.codeGenerator)
    private val helpersGenerator = HelpersGenerator(environment.codeGenerator)
    private val reducerGenerator = ReducerGenerator(environment.codeGenerator)
    private val eventGenerator = EventGenerator(environment.codeGenerator)
    private val stateMachineGenerator = StateMachineGenerator(environment.codeGenerator)

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        viewStateGenerator.generateViewState(classDeclaration)
        viewActionsGenerator.generateActions(classDeclaration)
        eventGenerator.generateEvents(classDeclaration)
        helpersGenerator.generateHelpers(classDeclaration)
        reducerGenerator.generateReducer(classDeclaration)
        stateMachineGenerator.generateStateMachine((classDeclaration))
    }
}
*/
