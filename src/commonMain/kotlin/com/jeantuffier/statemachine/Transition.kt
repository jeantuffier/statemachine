package com.jeantuffier.statemachine

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

data class Transition<STATE, EVENT>(
    /**
     * A lambda used to determine if the transition should be executed or not.
     * It has a state parameter as input, it should be the current state of the
     * state machine. A typical use case would be to check if data has already
     * been loaded before executing the transition if its purpose is to load data.
     */
    val isExecutable: IsExecutable<STATE>,

    /**
     * A lambda executing the logic to transition the state machine from its
     * current state to the next one.
     */
    val execute: Execute<EVENT, STATE>,
)

fun interface IsExecutable<STATE> {
    operator fun invoke(state: STATE): Boolean
}

fun interface Execute<EVENT, STATE> {
    suspend operator fun invoke(event: EVENT, state: MutableStateFlow<STATE>): Unit
}

fun interface ExecuteSideEffect<EVENT, SIDE_EFFECT> {
    suspend operator fun invoke(event: EVENT, state: MutableSharedFlow<SIDE_EFFECT>): Unit
}