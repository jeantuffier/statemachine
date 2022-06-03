package com.jeantuffier.statemachine

import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    suspend fun reduce(event: Event)
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
    private val reducer: suspend StateMachineBuilder<ViewState, Event>.(Event) -> Unit
) : StateMachine<ViewState, Event> {
    @NativeCoroutineScope
    internal val coroutineScope = MainScope()

    internal val _state = MutableStateFlow(initialValue)
    override val state = _state.asStateFlow()

    override suspend fun reduce(event: Event) = reducer(event)
}

/**
 * A helper function checking if a transition can be executed.
 */
suspend fun <ViewState, Event, T : Event> StateMachineBuilder<ViewState, Event>.onEvent(
    transition: Transition<ViewState, T>,
    event: T,
) {
    if (transition.isExecutable(state.value)) {
        _state.update { transition.execute(state.value, event) }
    }
}
