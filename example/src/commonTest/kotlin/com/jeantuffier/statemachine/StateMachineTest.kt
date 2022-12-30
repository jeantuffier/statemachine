package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class StateMachineTest {

    @Test
    fun initialStateShouldBeCorrect() = runTest {
        SchoolStateMachine(scope = TestScope()).state.test {
            assertEquals(SchoolViewState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()
    }

    @Test
    fun loadDataShouldSucceed() = runTest {
        val schoolStateMachine = SchoolStateMachine(
            loadSchoolData = { state, _ ->
                state.copy(id = "1", name = "name")
            },
            scope = TestScope(),
        )
        schoolStateMachine.state.test() {
            assertEquals(SchoolViewState(), awaitItem())

            schoolStateMachine.reduce(SchoolViewEvents.LoadSchoolData)

            val next = awaitItem()
            assertEquals("1", next.id)
            assertEquals("name", next.name)
        }
        advanceUntilIdle()
    }

    /*@Test
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
