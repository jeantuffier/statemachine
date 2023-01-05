package com.jeantuffier.statemachine.framework

import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 * emit new states to the client after reducing an event.
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

    fun close()
}

/**
 * This class facilitate the implementation of a [StateMachine].
 * It already contains a [MutableStateFlow] to update the state machine state
 * and is used to override [StateMachine.state].
 *
 * @param initialValue: the value used to initialize the state.
 * @param scope: the coroutine scope used by the state machine to run sub coroutines
 * @param reducer: a function matching events with business logic to execute. The easiest way
 * to do so is by using a `when` statement.
 */

class StateMachineBuilder<ViewState, Event>(
    initialValue: ViewState,
    private val scope: CoroutineScope,
    private val reducer: suspend (MutableStateFlow<ViewState>, Event) -> Unit
) : StateMachine<ViewState, Event> {

    private val _state: MutableStateFlow<ViewState> = MutableStateFlow(initialValue)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override suspend fun <T : Event> reduce(event: T) {
        reducer(_state, event)
    }

    override fun close() {
        scope.cancel()
    }
}

typealias StateUpdate<ViewState, Event> = (ViewState, Event) -> ViewState
typealias AsyncDataUpdate<Event, Error, AsyncDataType> = suspend (Event) -> Either<Error, AsyncDataType>
