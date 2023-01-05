package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.framework.AsyncDataStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewStateExtensionsTest {

    @Test
    fun shouldSucceedLoadTeachers() = runTest {
        val event = object : LoadTeachersEvent {
            override val offset = 0
            override val limit = 20
        }
        val teachers = listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2"))

        val state = MutableStateFlow(SchoolViewState())
        state.test {
            var next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            state.loadTeachers(event) { Either.Right(teachers) }

            next = awaitItem()
            assertEquals(AsyncDataStatus.LOADING, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            next = awaitItem()
            assertEquals(AsyncDataStatus.SUCCESS, next.teachers.status)
            assertEquals(teachers, next.teachers.data)
        }
    }

    @Test
    fun shouldFailToLoadTeachers() = runTest {
        val event = object : LoadTeachersEvent {
            override val offset = 0
            override val limit = 20
        }

        val state = MutableStateFlow(SchoolViewState())
        state.test {
            var next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            state.loadTeachers(event) { Either.Left(SomeRandomError) }

            next = awaitItem()
            assertEquals(AsyncDataStatus.LOADING, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            next = awaitItem()
            assertEquals(AsyncDataStatus.ERROR, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
        }
    }
}