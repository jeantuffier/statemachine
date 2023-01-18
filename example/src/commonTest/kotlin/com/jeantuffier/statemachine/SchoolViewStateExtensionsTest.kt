package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.framework.AsyncDataStatus
import com.jeantuffier.statemachine.framework.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewStateExtensionsTest {

    @Test
    fun shouldSucceedLoadTeachers() = runTest {
        val action = object : LoadTeachersAction {
            override val offset = 0
            override val limit = 20
        }
        val teachers = listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2"))

        val state = MutableStateFlow(SchoolViewState())
        state.test {
            var next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            state.loadTeachers(action) { Either.Right(teachers) }

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
        val action = object : LoadTeachersAction {
            override val offset = 0
            override val limit = 20
        }

        val state = MutableStateFlow(SchoolViewState())
        state.test {
            var next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            state.loadTeachers(action) { Either.Left(SomeRandomError) }

            next = awaitItem()
            assertEquals(AsyncDataStatus.LOADING, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)

            next = awaitItem()
            assertEquals(AsyncDataStatus.ERROR, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
        }
    }

    object NavigationAction
    class NavigationUiEvent(override val id: String) : UiEvent

    @Test
    fun shouldUpdateUiEvents() = runTest {
        val state = MutableStateFlow(SchoolViewState())
        state.test {
            var next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
            assertTrue(next.uiEvents.isEmpty())

            val event = NavigationUiEvent("1")
            state.onUiEvents(NavigationAction) { event }

            next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
            assertTrue(next.uiEvents.size == 1)
            assertEquals(next.uiEvents.first(), event)
        }
    }

    @Test
    fun shouldHandleUiEvents() = runTest {
        val state = MutableStateFlow(SchoolViewState())
        state.test {
            var next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
            assertTrue(next.uiEvents.isEmpty())

            val event = NavigationUiEvent("1")
            state.onUiEvents(NavigationAction) { event }

            next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
            assertTrue(next.uiEvents.size == 1)
            assertEquals(next.uiEvents.first(), event)

            state.onUiEventsHandled(event)

            next = awaitItem()
            assertEquals(AsyncDataStatus.INITIAL, next.teachers.status)
            assertEquals(emptyList(), next.teachers.data)
            assertTrue(next.uiEvents.isEmpty())
        }
    }
}