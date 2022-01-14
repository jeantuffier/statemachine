package com.jeantuffier.statemachine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

internal class StateMachineImpl<STATE, EVENT, SIDE_EFFECT>(
    initialState: STATE,
    coroutineContext: CoroutineContext,
) : StateMachine<STATE, EVENT, SIDE_EFFECT> {

    private val supervisor = SupervisorJob()
    private val coroutineScope = CoroutineScope(coroutineContext + supervisor)
    private var runningJob: Job? = null

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<STATE> = _state

    private val _sideEffect = MutableSharedFlow<SIDE_EFFECT>()
    override val sideEffects: Flow<SIDE_EFFECT> = _sideEffect

    internal val transitions = mutableMapOf<String, Transition<STATE, EVENT>>()
    internal val transitionsWithSideEffect = mutableMapOf<String, TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>>()

    override fun register(eventName: String, transition: Transition<STATE, EVENT>) {
        transitions[eventName] = transition
    }

    override fun register(eventName: String, transition: TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>) {
        transitionsWithSideEffect[eventName] = transition
    }

    override fun <T : EVENT> onEvent(eventName: String, event: T) {
        findTransition(eventName)?.let {
            runningJob = coroutineScope.launch {
                if (it.isExecutable(state.value)) {
                    it.execute(event, _state)
                }
            }
            return
        }

        findSideEffectTransition(eventName)?.let {
            runningJob = coroutineScope.launch {
                if (it.isExecutable(state.value)) {
                    it.execute(event, _sideEffect)
                }
            }
        }
    }

    override fun destroy() {
        runningJob?.cancel()
        supervisor.cancel()
        coroutineScope.cancel()
    }
}


private fun <STATE, EVENT, SIDE_EFFECT> StateMachineImpl<STATE, EVENT, SIDE_EFFECT>.findTransition(
    eventName: String
): Transition<STATE, EVENT>? {
    return transitions.filter { it.key == eventName }
        .map { it.value }
        .firstOrNull()
}

private fun <STATE, EVENT, SIDE_EFFECT> StateMachineImpl<STATE, EVENT, SIDE_EFFECT>.findSideEffectTransition(
    eventName: String
): TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>? {
    return transitionsWithSideEffect.filter { it.key == eventName }
        .map { it.value }
        .firstOrNull()
}
