package com.jeantuffier.generator

import arrow.core.Either
import arrow.core.right
import com.jeantuffier.statemachine.orchestrate.Feature
import com.jeantuffier.statemachine.orchestrate.State
import com.jeantuffier.statemachine.orchestrate.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@UseCase
fun increment(counter: Int) = counter + 1

@UseCase
fun decrement(counter: Int) = counter - 1

@UseCase
suspend fun getFact(counter: Int): Either<Throwable, String> {
    delay(100)
    return "Some fact about $counter".right()
}

@Feature
interface Counter : CancellableUseCase {

    @State
    data class CounterState(
        val count: Int = 0,
        val fact: String? = null
    )

    @Update(CounterStateProperty.Count)
    @With(UseCases.Increment)
    fun onIncrementButtonTapped()

    @Update(CounterStateProperty.Count)
    @With(UseCases.Decrement)
    fun onDecrementButtonTapped()

    @Update(CounterStateProperty.Fact)
    @With(UseCases.GetFact)
    fun onFactButtonTapped()
}

class CounterStoreBluePrint(coroutineDispatcher: CoroutineDispatcher) : Counter {

    private val scope = CoroutineScope(SupervisorJob() + coroutineDispatcher)
    private val _state = MutableStateFlow(Counter.CounterState())
    val state = _state.asStateFlow()

    private val jobs = mutableMapOf<CancelID, Job>()

    override fun onIncrementButtonTapped() {
        _state.update {
            it.copy(count = increment(it.count))
        }
    }

    override fun onDecrementButtonTapped() {
        _state.update {
            it.copy(count = decrement(it.count))
        }
    }

    override fun onFactButtonTapped() {
        jobs[CancelID.GetFact]?.cancel()
        scope.launch {
            when (val fact = getFact(state.value.count)) {
                is Either.Left -> {}
                is Either.Right -> _state.update {
                    it.copy(fact = fact.value)
                }
            }
        }.also { jobs[CancelID.GetFact] = it }
    }

    override fun cancel(id: CancelID) {
        jobs[id]?.cancel()
    }
}
