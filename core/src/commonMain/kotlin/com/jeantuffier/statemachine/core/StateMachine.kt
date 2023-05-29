package com.jeantuffier.statemachine.core

import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
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
 * A [StateMachine] should be used to extract duplicated business logic into a common package.
 *
 * It is designed to work with a unidirectional pattern (UDF). The client should use input
 * objects to trigger the execution of specific logic and collect outputs from the state flow.
 *
 * The state machine exposes a [StateFlow] to retain its current state and be able to
 * emit new states to the client after reducing an input.
 */
interface StateMachine<Input, Output> {

    /**
     * Clients should collect from this state flow to receive new updates of the state.
     */
    val state: StateFlow<Output>

    /**
     * Clients should call this function whenever an [Input] is triggered by a user.
     * @param input: The input triggered by the user.
     */
    fun <T : Input> reduce(input: T)

    /**
     * Clients should call this function whenever an [Input] triggered by [reduce] needs to be cancelled.
     * @param input: The input used to trigger a job to should be cancelled.
     */
    fun <T : Input> cancel(input: T, rollback: StateUpdate<Output>)

    suspend fun close()
}

fun <Input : Any, Output> StateMachine(
    initialValue: Output,
    coroutineDispatcher: CoroutineDispatcher,
    reducer: Reducer<Input, Output>,
) = object : StateMachine<Input, Output> {

    private val job = SupervisorJob()

    @NativeCoroutineScope
    private val coroutineScope = CoroutineScope(job + coroutineDispatcher)

    private val _state = MutableStateFlow(initialValue)
    override val state = _state.asStateFlow()

    private val jobRegistry: MutableMap<String, Job> = mutableMapOf()

    override fun <T : Input> reduce(input: T) {
        jobRegistry[input::class.simpleName ?: ""]?.cancel()
        jobRegistry[input::class.simpleName ?: ""] = coroutineScope.launch {
            reducer(input)
                .cancellable()
                .collect {
                    _state.update { it(state.value) }
                }
        }
    }

    override fun <T : Input> cancel(input: T, rollback: StateUpdate<Output>) {
        jobRegistry[input::class.simpleName ?: ""]?.cancel()
        coroutineScope.launch {
            _state.update { rollback(state.value) }
        }
    }

    override suspend fun close() {
        job.cancelChildren()
        job.cancel()
        job.join()
    }
}

fun interface StateUpdate<T> {
    suspend operator fun invoke(state: T): T
}

fun interface Reducer<Input, Output> {
    suspend operator fun invoke(input: Input): Flow<StateUpdate<Output>>
}
