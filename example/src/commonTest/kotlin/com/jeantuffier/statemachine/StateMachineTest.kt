package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class StateMachineTest {

//    private lateinit var stateMachine1: StateMachine<ViewState1, Event>
//    private lateinit var stateMachine2: StateMachine<ViewState2, Event>

    /*@BeforeTest
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
    }*/
}
