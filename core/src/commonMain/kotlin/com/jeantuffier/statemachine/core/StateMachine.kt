package com.jeantuffier.statemachine.core

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Abstracts and centralizes the common business logic of your software.
 *
 * Trigger a specific action by using the [StateMachine.reduce] function and passing it the desired input.
 * Once the logic has been executed, the state will be updated accordingly.
 *
 * While direct implementation is possible, it is recommended to use the function [StateMachine] to obtain a
 * working instance out of the box.
 *
 */
interface StateMachine<Input, Output> {

    /**
     * The state of the state machine. Clients should collect from this flow to receive new updates.
     */
    @NativeCoroutinesState
    val state: StateFlow<Output>

    /**
     * Triggers the execution of an input.
     * @param input The input that needs to be executed by the state machine.
     */
    fun <T : Input> reduce(input: T)

    /**
     * Cancels the execution of a specific action.
     * @param input The input to cancel.
     * @param rollback A [StateUpdate] to restore the state machine's state after cancellation.
     */
    fun <T : Input> cancel(input: T, rollback: Effect.StateUpdate<Output>)

    /**
     * Cancels the state machine job and its children.
     */
    fun close()
}

/**
 * Creates an instance of [StateMachine].
 *
 * @param initialValue The initial value of the state machine's state.
 * @param coroutineDispatcher The coroutine dispatcher to use when executing logic triggered by [StateMachine.reduce].
 * @param reducer The reducer to use in this state machine.
 *
 * Each execution triggered by the [StateMachine.reduce] function is launched inside a dedicated coroutine. That gives
 * the state machine the possibility to parallelize executions.
 *
 * Whenever [StateMachine.reduce] is called, an entry is added to "jobRegistry". This give us the possibility to cancel
 * the execution based on the input hashcode later on. If an input is passed again to the reduce function before the
 * previous execution completed, it will cancel it and rerun the execution.
 *
 * When cancelling an input with [StateMachine.cancel], a [StateUpdate] is required to restore the state machine's
 * state. For example, in the situation where an input's execution has already updated the state before the cancel
 * function is called. The state will be stale if not restored properly, the rollback parameter is there to ensure the
 * state validity after cancellation.
 */
fun <Input : Any, Output> StateMachine(
    initialValue: Output,
    coroutineDispatcher: CoroutineDispatcher,
    reducer: Reducer<Input, Output>,
) = object : StateMachine<Input, Output> {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job + coroutineDispatcher)

    private val _state = MutableStateFlow(initialValue)
    override val state = _state.asStateFlow()

    private val jobRegistry: MutableMap<Int, Job> = mutableMapOf()

    override fun <T : Input> reduce(input: T) {
        input.hashCode()
        jobRegistry[input.hashCode()]?.cancel()
        jobRegistry[input.hashCode()] = coroutineScope.launch {
            reducer(input)
                .cancellable()
                .collect {
                    _state.update { it(state.value) }
                }
        }
    }

    override fun <T : Input> cancel(input: T, rollback: Effect.StateUpdate<Output>) {
        jobRegistry[input.hashCode()]?.cancel()
        coroutineScope.launch {
            _state.update { rollback(state.value) }
        }
    }

    override fun close() {
        coroutineScope.launch {
            job.cancelChildren()
            job.cancel()
            job.join()
        }
    }
}

/**
 * The function matching state machine's input and the logic to execute.
 */
fun interface Reducer<Input, Output> {

    /**
     * An input can potentially result in several state updates, for example when loading data. We usually want
     * to show a spinner or something equivalent to let the user know something is loading and then show the data when
     * it is available. For that reason, a [Reducer] returns a flow of [StateUpdate].
     */
    @NativeCoroutines
    suspend operator fun invoke(input: Input): Flow<Effect.StateUpdate<Output>>
}

sealed interface Effect {
    /**
     * The type returned by [Reducer] to represent how a state machine state should be updated.
     */
    fun interface StateUpdate<T>: Effect {

        /**
         * @param state: the current state hold by the state machine.
         * @return an updated state value.
         */
        suspend operator fun invoke(state: T): T
    }

    data object None : Effect
}
