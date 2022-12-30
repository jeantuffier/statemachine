package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.AsyncDataStatus
import com.jeantuffier.statemachine.framework.loadAsyncData
import com.jeantuffier.statemachine.framework.loadAsyncDataFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncDataTest {

    private val loadTeachersEvent = SchoolViewEvents.LoadTeachersEvent(0, 5)
    private lateinit var persons: AsyncData<List<Person>>

    @BeforeTest
    fun setUp() {
        persons = AsyncData(emptyList())
    }

    @Test
    fun loadAsyncDataShouldSucceed() = runTest {
        val personRange = generatePersons(0, 19)
        loadAsyncData(persons, loadTeachersEvent) {
            Either.Right(generatePersons(loadTeachersEvent.offset, loadTeachersEvent.limit))
        }.test {
            assertEquals(
                expected = persons.copy(status = AsyncDataStatus.LOADING),
                actual = awaitItem(),
            )

            assertEquals(
                expected = AsyncData(
                    data = personRange.subList(loadTeachersEvent.offset, loadTeachersEvent.limit),
                    status = AsyncDataStatus.SUCCESS,
                ),
                actual = awaitItem()
            )

            awaitComplete()
        }
    }

    @Test
    fun loadAsyncDataShouldFail() = runTest {
        loadAsyncData(persons, loadTeachersEvent) {
            Either.Left(SomeRandomError)
        }.test {
            assertEquals(
                expected = persons.copy(status = AsyncDataStatus.LOADING),
                actual = awaitItem(),
            )

            assertEquals(
                expected = AsyncData<List<Person>>(
                    data = null,
                    status = AsyncDataStatus.ERROR,
                ),
                actual = awaitItem()
            )

            awaitComplete()
        }
    }

    @Test
    fun loadAsyncDataFlowShouldSucceed() = runTest {
        val personRange = generatePersons(0, 19)
        loadAsyncDataFlow(persons, loadTeachersEvent) {
            flowOf(
                Either.Right(generatePersons(loadTeachersEvent.offset, loadTeachersEvent.limit - 1)),
                Either.Right(generatePersons(loadTeachersEvent.offset, loadTeachersEvent.limit))
            )
        }.test {
            assertEquals(
                expected = persons.copy(status = AsyncDataStatus.LOADING),
                actual = awaitItem(),
            )

            assertEquals(
                expected = AsyncData(
                    data = personRange.subList(loadTeachersEvent.offset, loadTeachersEvent.limit - 1),
                    status = AsyncDataStatus.SUCCESS,
                ),
                actual = awaitItem()
            )

            assertEquals(
                expected = AsyncData(
                    data = personRange.subList(loadTeachersEvent.offset, loadTeachersEvent.limit),
                    status = AsyncDataStatus.SUCCESS,
                ),
                actual = awaitItem()
            )

            awaitComplete()
        }
    }

    @Test
    fun loadAsyncDataFlowShouldFail() = runTest {
        val personRange = generatePersons(0, 19)
        loadAsyncDataFlow(persons, loadTeachersEvent) {
            flowOf(
                Either.Right(generatePersons(loadTeachersEvent.offset, loadTeachersEvent.limit - 1)),
                Either.Left(SomeRandomError)
            )
        }.test {
            assertEquals(
                expected = persons.copy(status = AsyncDataStatus.LOADING),
                actual = awaitItem(),
            )

            assertEquals(
                expected = AsyncData(
                    data = personRange.subList(loadTeachersEvent.offset, loadTeachersEvent.limit - 1),
                    status = AsyncDataStatus.SUCCESS,
                ),
                actual = awaitItem()
            )

            assertEquals(
                expected = AsyncData<List<Person>>(
                    data = null,
                    status = AsyncDataStatus.ERROR,
                ),
                actual = awaitItem()
            )

            awaitComplete()
        }
    }

    private fun generatePersons(offset: Int, limit: Int) =
        IntRange(offset, limit - 1).map {
            Person("firstName$it", "lastName$it")
        }
}