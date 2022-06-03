package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

sealed class Event {
    data class Event1(val value: Int) : Event()
}

data class ViewState(val counter: Int = 0)

class Transition1 : Transition<ViewState, Event.Event1> by TransitionBuilder(
    predicate = { it.counter < 5 },
    execution = { state, event ->
        state.copy(counter = state.counter + event.value)
    }
)

class ViewStateMachine(
    private val transition1: Transition1
) : StateMachine<ViewState, Event> by StateMachineBuilder(
    initialValue = ViewState(),
    reducer = {
        when (it) {
            is Event.Event1 -> onEvent(transition1, it)
        }
    }
)

@ExperimentalTime
@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var stateMachine: StateMachine<ViewState, Event>

    @BeforeTest
    fun setUp() {
        stateMachine = ViewStateMachine(Transition1())
    }

    @Test
    fun ensureInitialDataIsCorrect() = runBlockingTest {
        stateMachine.state.test {
            assertEquals(ViewState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
