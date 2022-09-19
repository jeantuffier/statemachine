package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var schoolStateMachine: SchoolStateMachine

    @BeforeTest
    fun setUp() {
        schoolStateMachine = SchoolStateMachine()
    }

    @Test
    fun ensureInitialDataIsCorrect() = runBlockingTest {
        schoolStateMachine.state.test {
            assertEquals(SchoolViewState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading() = runBlockingTest {
        val flow: Flow<SchoolViewState> = schoolStateMachine.state
        flow.test {
            assertEquals(SchoolViewState(), awaitItem())

            schoolStateMachine.reduce(SchoolViewEvents.LoadStudents(0, 20))
            assertEquals(AsyncDataStatus.LOADING, awaitItem().students.status)

            val next = awaitItem()
            assertEquals(AsyncDataStatus.SUCCESS, next.students.status)
            assertEquals(
                listOf(
                    Person("student1", "student1"),
                    Person("student2", "student2")
                ),
                next.students.data,
            )
        }
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
