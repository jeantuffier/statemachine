package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

sealed class Event {
    data class Event1(val value: Int) : Event()
    object Event2 : Event()
}

data class ViewState(
    val counter: Int = 0,
    val remoteValue: AsyncData<String> = AsyncData(value = "")
)

val transition1 = Transition<ViewState, Event.Event1> { state, event ->
    if (state.value.counter < 5) {
        state.value = state.value.copy(counter = state.value.counter + event.value)
    }
}

val transition2 = Transition<ViewState, Event.Event2> { state, status ->
    state.value = state.value.copy(
        remoteValue = state.value.remoteValue.copy(
            status = AsyncDataStatus.LOADING,
        )
    )
    state.value = state.value.copy(
        remoteValue = state.value.remoteValue.copy(
            status = AsyncDataStatus.LOADED,
            value = "remote value",
        )
    )
}

class ViewStateMachine : StateMachine<ViewState, Event> by StateMachineBuilder(
    initialValue = ViewState(),
    reducer = { state, event ->
        when (event) {
            is Event.Event1 -> transition1(state, event)
            is Event.Event2 -> transition2(state, event)
        }
    }
)

@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var stateMachine: StateMachine<ViewState, Event>

    @BeforeTest
    fun setUp() {
        stateMachine = ViewStateMachine()
    }

    @Test
    fun ensureInitialDataIsCorrect() = runBlockingTest {
        stateMachine.state.test {
            assertEquals(ViewState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun immediateTransactionShouldSucceed() = runBlockingTest {
        val flow: Flow<ViewState> = stateMachine.state
        flow.test {
            assertEquals(ViewState(), awaitItem())

            stateMachine.reduce(Event.Event1(2))
            assertEquals(2, awaitItem().counter)

            stateMachine.reduce(Event.Event1(3))
            assertEquals(5, awaitItem().counter)

            // nothing should be emitted, if it does, the test should fail with "Unconsumed events found"
            stateMachine.reduce(Event.Event1(2))
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun asyncTransitionShouldSucceed() = runBlockingTest {
        val flow: Flow<ViewState> = stateMachine.state
        flow.test {
            assertEquals(ViewState(), awaitItem())

            stateMachine.reduce(Event.Event2)
            assertEquals(AsyncDataStatus.LOADING, awaitItem().remoteValue.status)

            val next = awaitItem()
            assertEquals(AsyncDataStatus.LOADED, next.remoteValue.status)
            assertEquals("remote value", next.remoteValue.value)
        }
    }
}
