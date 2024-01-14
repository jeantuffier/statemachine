package com.jeantuffier.generator

import arrow.core.Either
import arrow.core.right
import com.jeantuffier.statemachine.orchestrate.Feature
import com.jeantuffier.statemachine.orchestrate.State
import com.jeantuffier.statemachine.orchestrate.StateUpdater
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@StateUpdater
fun Counter.increment() = state.value.copy(count = state.value.count + 1)

@StateUpdater
fun Counter.decrement() = state.value.copy(count = state.value.count - 1)

@StateUpdater
suspend fun Counter.getFact(): Flow<Counter.CounterState> = flow {
    emit(state.value.copy(isLoading = true))
    val fact = getFact(state.value.count)
    emit(
        when (fact) {
            is Either.Left -> state.value.copy(error = fact.value.message)
            is Either.Right -> state.value.copy(fact = fact.value)
        }
    )
}

suspend fun getFact(counter: Int): Either<Throwable, String> {
    delay(100)
    return "Some fact about $counter".right()
}

@Feature
interface Counter : CancellableStateUpdater {

    @State
    data class CounterState(
        val count: Int = 0,
        val isLoading: Boolean = false,
        val fact: String? = null,
        val error: String? = null,
    )

    val state: StateFlow<CounterState>

    @With(StateUpdaterID.Increment)
    fun onIncrementButtonTapped()

    @With(StateUpdaterID.Decrement)
    fun onDecrementButtonTapped()

    @With(StateUpdaterID.GetFact)
    fun onFactButtonTapped()
}

class CounterStoreBluePrint(coroutineDispatcher: CoroutineDispatcher) : Counter {

    private val scope = CoroutineScope(SupervisorJob() + coroutineDispatcher)
    private val jobs = mutableMapOf<StateUpdaterID, Job>()

    private val _state = MutableStateFlow(Counter.CounterState())
    override val state = _state.asStateFlow()

    override fun onIncrementButtonTapped() {
        _state.update { increment() }
    }

    override fun onDecrementButtonTapped() {
        _state.update { decrement() }
    }

    override fun onFactButtonTapped() {
        jobs[StateUpdaterID.GetFact]?.cancel()
        scope.launch {
            getFact().collect {
                _state.update { it }
            }
        }.also { jobs[StateUpdaterID.GetFact] = it }
    }

    override fun cancel(id: StateUpdaterID) {
        jobs[id]?.cancel()
    }
}
