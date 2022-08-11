package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

sealed class Event {
    data class IsLoading(val value: Boolean) : Event()

    data class UpdateCounter(val value: Int) : Event()
    object LoadRemoteValue : Event()
}

data class ViewState1(
    val isLoading: Boolean = false,
    val counter: Int = 0,
    val remoteValue: String = "",
)

data class ViewState2(
    val isLoading: Boolean = false,
    val counter: Int = 0,
    val remoteValue: String = "",
)

enum class TransitionKey { IS_LOADING, COUNTER, REMOVE_VALUE }

class ViewState1Updater(
    private val mutableStateFlow: MutableStateFlow<ViewState1>,
) : ViewStateUpdater<TransitionKey> {

    override fun <T> currentValue(key: TransitionKey) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.value.isLoading as T
            TransitionKey.COUNTER -> mutableStateFlow.value.counter as T
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.value.remoteValue as T
        }

    override fun updateValue(key: TransitionKey, newValue: Any) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.update { it.copy(isLoading = newValue as Boolean) }
            TransitionKey.COUNTER -> mutableStateFlow.update { it.copy(counter = newValue as Int) }
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.update { it.copy(remoteValue = newValue as String) }
        }

    override fun updateValues(values: Map<TransitionKey, Any>) =
        values.entries.forEach { updateValue(it.key, it.value) }
}

class ViewState2Updater(
    private val mutableStateFlow: MutableStateFlow<ViewState2>,
) : ViewStateUpdater<TransitionKey> {

    override fun <T> currentValue(key: TransitionKey) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.value.isLoading as T
            TransitionKey.COUNTER -> mutableStateFlow.value.counter as T
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.value.remoteValue as T
        }

    override fun updateValue(key: TransitionKey, newValue: Any) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.update { it.copy(isLoading = newValue as Boolean) }
            TransitionKey.COUNTER -> mutableStateFlow.update { it.copy(counter = newValue as Int) }
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.update { it.copy(remoteValue = newValue as String) }
        }

    override fun updateValues(values: Map<TransitionKey, Any>) =
        values.entries.forEach { updateValue(it.key, it.value) }
}

@SharedImmutable
val loadingTransition = Transition<Event.IsLoading, TransitionKey> { updater, event ->
    updater.updateValue(TransitionKey.IS_LOADING, event.value)
}

@SharedImmutable
val updateCounterTransition = Transition<Event.UpdateCounter, TransitionKey> { updater, event ->
    val counter = updater.currentValue<Int>(TransitionKey.COUNTER)
    if (counter < 5) {
        updater.updateValue(TransitionKey.COUNTER, counter + event.value)
    }
}

@SharedImmutable
val loadRemoteValueTransition = Transition<Event.LoadRemoteValue, TransitionKey> { updater, event ->
    updater.updateValue(TransitionKey.IS_LOADING, true)
    delay(3000)
    updater.updateValues(
        mapOf(
            TransitionKey.IS_LOADING to false,
            TransitionKey.REMOVE_VALUE to "remote value"
        )
    )
}

class ViewStateMachine1 : StateMachine<ViewState1, Event> by StateMachineBuilder(
    initialValue = ViewState1(),
    reducer = { state, event ->
        val updater = ViewState1Updater(state)
        when (event) {
            is Event.IsLoading -> loadingTransition(updater, event)
            is Event.UpdateCounter -> updateCounterTransition(updater, event)
            is Event.LoadRemoteValue -> loadRemoteValueTransition(updater, event)
        }
    }
)

class ViewStateMachine2 : StateMachine<ViewState2, Event> by StateMachineBuilder(
    initialValue = ViewState2(),
    reducer = { state, event ->
        val updater = ViewState2Updater(state)
        when (event) {
            is Event.IsLoading -> loadingTransition(updater, event)
            is Event.UpdateCounter -> updateCounterTransition(updater, event)
            is Event.LoadRemoteValue -> loadRemoteValueTransition(updater, event)
        }
    }
)

@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var stateMachine1: StateMachine<ViewState1, Event>
    private lateinit var stateMachine2: StateMachine<ViewState2, Event>

    @BeforeTest
    fun setUp() {
        stateMachine1 = ViewStateMachine1()
        stateMachine2 = ViewStateMachine2()
    }

    @Test
    fun ensureInitialDataIsCorrect1() = runBlockingTest {
        stateMachine1.state.test {
            assertEquals(ViewState1(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun ensureInitialDataIsCorrect2() = runBlockingTest {
        stateMachine2.state.test {
            assertEquals(ViewState2(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading1() = runBlockingTest {
        val flow: Flow<ViewState1> = stateMachine1.state
        flow.test {
            assertEquals(ViewState1(), awaitItem())

            stateMachine1.reduce(Event.IsLoading(true))
            assertEquals(true, awaitItem().isLoading)

            stateMachine1.reduce(Event.IsLoading(false))
            assertEquals(false, awaitItem().isLoading)
        }
    }

    @Test
    fun isLoading2() = runBlockingTest {
        val flow: Flow<ViewState2> = stateMachine2.state
        flow.test {
            assertEquals(ViewState2(), awaitItem())

            stateMachine2.reduce(Event.IsLoading(true))
            assertEquals(true, awaitItem().isLoading)

            stateMachine2.reduce(Event.IsLoading(false))
            assertEquals(false, awaitItem().isLoading)
        }
    }

    @Test
    fun counter1() = runBlockingTest {
        val flow: Flow<ViewState1> = stateMachine1.state
        flow.test {
            assertEquals(ViewState1(), awaitItem())

            stateMachine1.reduce(Event.UpdateCounter(2))
            assertEquals(2, awaitItem().counter)

            stateMachine1.reduce(Event.UpdateCounter(3))
            assertEquals(5, awaitItem().counter)

            stateMachine1.reduce(Event.UpdateCounter(1))
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun counter2() = runBlockingTest {
        val flow: Flow<ViewState2> = stateMachine2.state
        flow.test {
            assertEquals(ViewState2(), awaitItem())

            stateMachine2.reduce(Event.UpdateCounter(2))
            assertEquals(2, awaitItem().counter)

            stateMachine2.reduce(Event.UpdateCounter(3))
            assertEquals(5, awaitItem().counter)

            stateMachine2.reduce(Event.UpdateCounter(1))
            expectNoEvents()
            cancel()
        }
    }
}
