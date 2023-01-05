package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.framework.AsyncDataStatus
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
        SchoolStateMachine.create().state.test {
            assertEquals(SchoolViewState(), awaitItem())
        }
    }

    @Test
    fun loadDataShouldSucceed() = runTest {
        val schoolStateMachine = SchoolStateMachine.create(
            loadSchoolData = { state, _ -> state.copy(id = "1", name = "name") },
        )
        schoolStateMachine.state.test {
            assertEquals(SchoolViewState(), awaitItem())

            schoolStateMachine.reduce(SchoolViewEvents.LoadSchoolData)

            val next = awaitItem()
            assertEquals("1", next.id)
            assertEquals("name", next.name)
        }
    }

    @Test
    fun loadStudentsShouldSucceed() = runTest {
        val students = listOf(Person("student1", "student1"), Person("student2", "student2"))
        val schoolStateMachine = SchoolStateMachine.create(
            loadStudent = { event -> Either.Right(students) },
        )
        schoolStateMachine.state.test {
            assertEquals(SchoolViewState(), awaitItem())

            schoolStateMachine.reduce(SchoolViewEvents.LoadStudentsEvent(0, 5))

            var next = awaitItem()
            assertEquals(AsyncDataStatus.LOADING, next.students.status)
            assertEquals(emptyList(), next.students.data)

            next = awaitItem()
            assertEquals(AsyncDataStatus.SUCCESS, next.students.status)
            assertEquals(students, next.students.data)
        }
    }

    @Test
    fun loadStudentShouldFail() = runTest {
        val schoolStateMachine = SchoolStateMachine.create(
            loadStudent = { event -> Either.Left(SomeRandomError) },
        )
        schoolStateMachine.state.test {
            assertEquals(SchoolViewState(), awaitItem())

            schoolStateMachine.reduce(SchoolViewEvents.LoadStudentsEvent(0, 5))

            var next = awaitItem()
            assertEquals(AsyncDataStatus.LOADING, next.students.status)
            assertEquals(emptyList(), next.students.data)

            next = awaitItem()
            assertEquals(AsyncDataStatus.ERROR, next.students.status)
            assertEquals(emptyList(), next.students.data)
        }
    }
}
