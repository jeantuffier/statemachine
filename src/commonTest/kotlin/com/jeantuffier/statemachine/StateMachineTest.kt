package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

sealed class Event {
    data class Event1(val value: Int) : Event()
    data class Event2(val value: Int) : Event()
}

data class ViewState(val counter: Int = 0)

val transition1 = Transition<ViewState, Event.Event1> { state, event ->
    if (state.counter < 5) {
        state.copy(counter = state.counter + event.value)
    } else state
}

val transition2 = Transition<ViewState, Event.Event2> { state, event ->
    if (state.counter < 20) {
        state.copy(counter = state.counter * event.value)
    } else state
}

class ViewStateMachine(
    private val transition1: Transition<ViewState, Event.Event1>,
    private val transition2: Transition<ViewState, Event.Event2>,
) : StateMachine<ViewState, Event> by StateMachineBuilder(
    initialValue = ViewState(),
    reducer = {
        when (it) {
            is Event.Event1 -> executeTransition(transition1, it)
            is Event.Event2 -> executeTransition(transition2, it)
        }
    }
)

@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var stateMachine: StateMachine<ViewState, Event>

    @BeforeTest
    fun setUp() {
        stateMachine = ViewStateMachine(transition1, transition2)
    }

    @Test
    fun ensureInitialDataIsCorrect() = runBlockingTest {
        stateMachine.state.test {
            assertEquals(ViewState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun ensurePredicateIsRespected() = runBlockingTest {
        stateMachine.state.test {
            assertEquals(ViewState(), awaitItem())

            stateMachine.reduce(Event.Event1(2))
            assertEquals(2, awaitItem().counter)

            stateMachine.reduce(Event.Event1(3))
            assertEquals(5, awaitItem().counter)

            // nothing should be emitted, if it does, the test should fail with "Unconsumed events found"
            stateMachine.reduce(Event.Event1(2))
        }
    }
}
