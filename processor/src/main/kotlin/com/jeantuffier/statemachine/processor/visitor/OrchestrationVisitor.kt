package com.jeantuffier.statemachine.processor.visitor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.jeantuffier.statemachine.processor.generator.ActionsGenerator
import com.jeantuffier.statemachine.processor.generator.HelpersGenerator
import com.jeantuffier.statemachine.processor.generator.ReducerGenerator
import com.jeantuffier.statemachine.processor.generator.SideEffectGenerator
import com.jeantuffier.statemachine.processor.generator.StateGenerator
import com.jeantuffier.statemachine.processor.generator.StateMachineGenerator

class OrchestrationVisitor(environment: SymbolProcessorEnvironment) : KSVisitorVoid() {

    private val viewStateGenerator = StateGenerator(environment.logger, environment.codeGenerator)
    private val viewActionsGenerator = ActionsGenerator(environment.logger, environment.codeGenerator)
    private val helpersGenerator = HelpersGenerator(environment.logger, environment.codeGenerator)
    private val reducerGenerator = ReducerGenerator(environment.logger, environment.codeGenerator)
    private val sideEffectGenerator = SideEffectGenerator(environment.logger, environment.codeGenerator)
    private val stateMachineGenerator = StateMachineGenerator(environment.logger, environment.codeGenerator)

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        viewStateGenerator.generateViewState(classDeclaration)
        viewActionsGenerator.generateActions(classDeclaration)
        sideEffectGenerator.generateSideEffects(classDeclaration)
        helpersGenerator.generateHelpers(classDeclaration)
        reducerGenerator.generateReducer(classDeclaration)
        stateMachineGenerator.generateStateMachine((classDeclaration))
    }
}
