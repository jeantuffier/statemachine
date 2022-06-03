package com.jeantuffier.statemachine

/**
 * A [Transition] abstracts the logical operations required for changing the
 * state of the machine into its nest state, based on a specific [Event].
 */
interface Transition<State, Event> {
    fun isExecutable(state: State): Boolean
    suspend fun execute(state: State, event: Event): State
}

/**
 * Abstracts the logic to check if a given transition should be executed.
 * A typical use case would be to check if the current state represents a loading
 * state before executing a new transition.
 */
fun interface IsExecutable<State> {

    /**
     * @param state: the current state of the state machine.
     */
    operator fun invoke(state: State): Boolean
}

/**
 * Abstracts the logic to transition the state machine from its current state to
 * the next one.
 */
fun interface Execute<State, Event> {

    /**
     * @param state: the current state of the state machine.
     * @param event: the event that triggers the transition.
     */
    suspend operator fun invoke(state: State, event: Event): State
}

internal class TransitionBuilder<State, Event>(
    private val predicate: IsExecutable<State>,
    private val execution: Execute<State, Event>
) : Transition<State, Event> {
    override fun isExecutable(state: State) = predicate(state)
    override suspend fun execute(state: State, event: Event): State = execution(state, event)
}
