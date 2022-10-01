package com.jeantuffier.statemachine.framework

import kotlinx.coroutines.flow.*

/**
 * A [StateMachine] should be used to extract the business logic required in a client
 * into the common package of a KMM library.
 *
 * It is design to work with a unidirectional pattern. The client should use [Event]
 * objects to represent any interaction between a user and the client. Those events
 * are then reduced into resulting [ViewState] that the client should use to render
 * its UI.
 *
 * The state machine exposes a [StateFlow] to retain its current state and be able to
 * emit new states to the client after executing a [Transition].
 */
interface StateMachine<ViewState, Event> {

    /**
     * Clients should collect from this state flow to receive new updates of the state.
     */
    val state: StateFlow<ViewState>

    /**
     * Clients should call this function whenever an [Event] is triggered by a user.
     * @param event: The event triggered by the user.
     */
    suspend fun <T : Event> reduce(event: T)
}

/**
 * This class facilitate the implementation of a [StateMachine].
 * It already contains a [MutableStateFlow] to update the state machine state
 * and is used to override [StateMachine.state].
 * It also has a [StateMachineBuilder.onEvent] extension allowing simple
 * matching between events and transitions.
 *
 * @param initialValue: the value used to initialize the state.
 * @param reducer: a function matching events with transition. The easiest way
 * to do so is by using a `when` statement.
 */
class StateMachineBuilder<ViewState, Event>(
    initialValue: ViewState,
    private val reducer: suspend (MutableStateFlow<ViewState>, Event) -> Unit
) : StateMachine<ViewState, Event> {

    private val _state = MutableStateFlow(initialValue)
    override val state = _state.asStateFlow()

    override suspend fun <T : Event> reduce(event: T) = reducer(_state, event)
}
