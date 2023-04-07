package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.orchestrate.Available
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedSideEffect
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import com.jeantuffier.statemachine.orchestrate.hasLoadedEverything
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieScreenTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun shouldBeInitialState() = testScope.runTest {
        defaultStateMachine(coroutineContext = testDispatcher).state.test {
            assertEquals(MovieScreenState(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun shouldLoadData() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(coroutineContext = testDispatcher)
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.LoadData("1"))

            var next = awaitItem()
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertEquals(
                listOf(
                    Actor("actor1", "actor1"),
                    Actor("actor2", "actor2"),
                    Actor("actor3", "actor3"),
                ),
                next.actors.items
            )
            assertTrue(next.actors.hasLoadedEverything())
        }
    }

    @Test
    fun shouldFailToLoadMovie() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(
            movie = { _ -> Either.Left(AppError.SomeRandomError) },
            coroutineContext = testDispatcher,
        )
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.LoadData("1"))

            var next = awaitItem()
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(1, next.sideEffects.size)
            assertEquals(null, next.movie.value)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            next = awaitItem()
            assertFalse(next.actors.isLoading)
            assertEquals(
                listOf(
                    Actor("actor1", "actor1"),
                    Actor("actor2", "actor2"),
                    Actor("actor3", "actor3"),
                ),
                next.actors.items
            )
            assertTrue(next.actors.hasLoadedEverything())
        }
    }

    @Test
    fun shouldFailToLoadActors() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(
            actors = { _ -> Either.Left(AppError.SomeRandomError) },
            coroutineContext = testDispatcher,
        )
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.LoadData("1"))

            var next = awaitItem()
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadActors)
            assertFalse(next.actors.hasLoadedEverything())
        }
    }

    @Test
    fun shouldLoadComments() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(coroutineContext = testDispatcher)
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.LoadComments("1", 0, 0))

            var next = awaitItem()
            assertTrue(next.comments.isLoading)
            assertTrue(next.comments.items.isEmpty())

            next = awaitItem()
            assertFalse(next.actors.isLoading)
            assertEquals(
                listOf(
                    Comment("comment1", "content1"),
                    Comment("comment2", "content2"),
                    Comment("comment3", "content3"),
                ),
                next.comments.items
            )
            assertTrue(next.comments.hasLoadedEverything())
        }
    }

    @Test
    fun shouldFailToLoadComments() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(
            comments = { _ -> flowOf(Either.Left(AppError.SomeRandomError)) },
            coroutineContext = testDispatcher,
        )
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.LoadComments("1", 0, 0))

            var next = awaitItem()
            assertTrue(next.comments.isLoading)
            assertTrue(next.comments.items.isEmpty())

            next = awaitItem()
            assertFalse(next.comments.isLoading)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.actors.items.isEmpty())
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadComments)
            assertFalse(next.comments.hasLoadedEverything())
        }
    }

    @Test
    fun saveAsFavoriteShouldSucceed() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(coroutineContext = testDispatcher)
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.SaveAsFavorite("1"))

            var next = awaitItem()
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            defaultStateMachine.reduce(MovieScreenAction.SideEffectHandled(next.sideEffects.first()))
            next = awaitItem()
            assertEquals(MovieScreenState(), next)

            next = awaitItem()
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.SaveAsFavoriteSucceeded)
        }
    }

    @Test
    fun saveAsFavoriteShouldFail() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(
            saveAsFavorite = { Either.Left(AppError.SomeRandomError) },
            coroutineContext = testDispatcher,
        )
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.SaveAsFavorite("1"))

            var next = awaitItem()
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            next = awaitItem()
            assertEquals(2, next.sideEffects.size)
            assertTrue(next.sideEffects[1] is MovieScreenSideEffects.SaveAsFavoriteFailed)
        }
    }

    @Test
    fun handleSideEffectShouldSucceed() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(coroutineContext = testDispatcher)
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.SaveAsFavorite("1"))

            var next = awaitItem()
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            next = awaitItem()
            assertEquals(2, next.sideEffects.size)
            assertTrue(next.sideEffects[1] is MovieScreenSideEffects.SaveAsFavoriteSucceeded)
        }
    }

    private fun defaultStateMachine(
        movie: OrchestratedUpdate<LoadData, AppError, Movie> = OrchestratedUpdate {
            Either.Right(
                Movie(
                    "1",
                    "Movie1"
                )
            )
        },
        actors: OrchestratedUpdate<LoadData, AppError, Page<Actor>> = OrchestratedUpdate {
            Either.Right(
                Page(
                    offset = Offset(0),
                    limit = Limit(0),
                    available = Available(3),
                    items = listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                )
            )
        },
        comments: OrchestratedFlowUpdate<LoadComments, AppError, Page<Comment>> = OrchestratedFlowUpdate {
            flowOf(
                Either.Right(
                    Page(
                        offset = Offset(0),
                        limit = Limit(0),
                        available = Available(3),
                        items = listOf(
                            Comment("comment1", "content1"),
                            Comment("comment2", "content2"),
                            Comment("comment3", "content3"),
                        ),

                        )
                )
            )
        },
        saveAsFavorite: OrchestratedSideEffect<SaveAsFavorite, AppError> = OrchestratedSideEffect {
            delay(100)
            Either.Right(Unit)
        },
        coroutineContext: CoroutineContext,
    ) = movieScreenStateMachine(
        movie,
        actors,
        comments,
        saveAsFavorite,
        coroutineContext,
    )
}
