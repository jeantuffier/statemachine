package com.jeantuffier.statemachine.core

import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext

/**
 * A [StateMachine] should be used to extract duplicated business logic into a common package.
 *
 * It is designed to work with a unidirectional pattern (UDF). The client should use [Input]
 * objects to trigger the execution of specific logic and collect [Ouput] from the state flow.
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
     * Clients should call this function whenever an [Action] is triggered by a user.
     * @param input: The input triggered by the user.
     */
    fun <T : Input> reduce(input: T)

    suspend fun close()
}

/**
 * This class facilitate the implementation of a [StateMachine].
 * It already contains a [MutableStateFlow] to update the state machine state
 * and is used to override [StateMachine.state].
 *
 * @param initialValue: the value used to initialize the state.
 * @param scope: the coroutine scope used by the state machine to run sub coroutines
 * @param stateMachineCore: a function matching inputs with business logic to execute. The easiest way
 * to do so is by using a `when` statement.
 */

/*class StateMachineBuilder<Input, Output>(
    initialValue: Output,
    coroutineContext: CoroutineContext,
    private val reducer: Reducer<Input, Output>,
) : StateMachine<Input, Output> {

    private val job = SupervisorJob()

    @NativeCoroutineScope
    private val coroutineScope = CoroutineScope(job + coroutineContext)

    private val _state: MutableStateFlow<Output> = MutableStateFlow(initialValue)
    override val state: StateFlow<Output> = _state.asStateFlow()

    override fun <T : Input> reduce(input: T) {
        coroutineScope.launch(job) { reducer(input, _state) }
    }

    override suspend fun close() {
        job.cancel()
        job.join()
    }
}*/

class StateMachineConfig(
    val sendCapacity: Int = Channel.UNLIMITED,
    val receiveCapacity: Int = 10,
)

@OptIn(ExperimentalCoroutinesApi::class)
fun <Input, Output> StateMachine(
    initialValue: Output,
    coroutineContext: CoroutineContext,
    reducer: Reducer<Input, Output>,
    stateMachineConfig: StateMachineConfig = StateMachineConfig()
) = object : StateMachine<Input, Output> {

    private val job = SupervisorJob()

    @NativeCoroutineScope
    private val coroutineScope = CoroutineScope(job + coroutineContext)

    private val inputChannel = Channel<Input>()

    private val _state = MutableStateFlow(initialValue)
    override val state = _state.asStateFlow()

    init {
        coroutineScope.launch {
            val stateUpdateChannel = produceStateUpdate()
            repeat(stateMachineConfig.receiveCapacity) { launchStateUpdateProcessor(stateUpdateChannel) }
        }
    }

    private fun CoroutineScope.produceStateUpdate(): ReceiveChannel<StateUpdate<Output>> =
        produce(capacity = stateMachineConfig.sendCapacity) {
            for (input in inputChannel) {
                launch {
                    reducer(input).collect {
                        send(it)
                    }
                }
            }
        }

    private fun CoroutineScope.launchStateUpdateProcessor(
        channel: ReceiveChannel<StateUpdate<Output>>
    ) = launch {
        for (stateUpdate in channel) {
            _state.update { stateUpdate(state.value) }
        }
    }

    override fun <T : Input> reduce(input: T) {
        coroutineScope.launch {
            inputChannel.send(input)
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
