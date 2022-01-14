package com.jeantuffier.statemachine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StateMachine<STATE, EVENT, SIDE_EFFECT> {
    /**
     * The state flow holding the state of the machine.
     */
    val state: StateFlow<STATE>

    /**
     * The state flow holding the state of the machine.
     */
    val sideEffects: Flow<SIDE_EFFECT>

    /**
     * Register a transition associated to an event name.
     * @param eventName: the name of the event that should trigger
     * a specific transition.
     * @param transition: the transition to register.
     */
    fun register(eventName: String, transition: Transition<STATE, EVENT>)

    /**
     * Register a side effect transition associated to an event name.
     * @param eventName: the name of the event that should trigger
     * a specific transition.
     * @param transition: the transition to register.
     */
    fun register(eventName: String, transition: TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>)

    /**
     * When called, the state machine will check if there are any registered
     * transition associated to that event and if it can be executed. If both
     * conditions are met, the state machine will execute the transition.
     * @param event: the event to use to trigger a transition.
     * @throws : CannotApplyEventError when the event passed as parameter cannot be applied
     * to the current state in which the state machine is.
     */
    fun <T : EVENT> onEvent(eventName: String, event: T)

    /**
     * Clean up resources.
     */
    fun destroy()
}
