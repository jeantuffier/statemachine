package com.jeantuffier.statemachine.movieScreen

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.Actor
import com.jeantuffier.statemachine.AppError
import com.jeantuffier.statemachine.Comment
import com.jeantuffier.statemachine.LoadComments
import com.jeantuffier.statemachine.LoadData
import com.jeantuffier.statemachine.Movie
import com.jeantuffier.statemachine.SaveAsFavorite
import com.jeantuffier.statemachine.orchestrate.Available
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedSideEffect
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import com.jeantuffier.statemachine.sideeffects.ActorsSideEffects
import com.jeantuffier.statemachine.sideeffects.CommentsSideEffects
import com.jeantuffier.statemachine.sideeffects.MovieSideEffects
import com.jeantuffier.statemachine.sideeffects.SaveAsFavoriteSideEffects
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

            defaultStateMachine.reduce(MovieScreenAction.LoadData("1", 0, 10))

            var next = awaitItem()
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                next.actors.pages,
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

            defaultStateMachine.reduce(MovieScreenAction.LoadData("1", 0, 10))

            var next = awaitItem()
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(1, next.sideEffects.size)
            assertEquals(null, next.movie.value)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieSideEffects.CouldNotBeLoaded)

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieSideEffects.CouldNotBeLoaded)

            next = awaitItem()
            assertFalse(next.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                next.actors.pages,
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

            defaultStateMachine.reduce(MovieScreenAction.LoadData("1", offset = 0, limit = 10))

            var next = awaitItem()
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), next.movie.value)
            assertFalse(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is ActorsSideEffects.CouldNotBeLoaded)
            assertFalse(next.actors.hasLoadedEverything())
        }
    }

    @Test
    fun shouldLoadComments() = testScope.runTest {
        val defaultStateMachine = defaultStateMachine(coroutineContext = testDispatcher)
        defaultStateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            defaultStateMachine.reduce(MovieScreenAction.LoadComments("1", 0, 10))

            var next = awaitItem()
            assertTrue(next.comments.isLoading)
            assertTrue(next.comments.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Comment("comment1", "content1"),
                        Comment("comment2", "content2"),
                        Comment("comment3", "content3"),
                    ),
                ),
                next.comments.pages,
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
            assertTrue(next.comments.pages.isEmpty())

            next = awaitItem()
            assertFalse(next.comments.isLoading)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.actors.pages.isEmpty())
            assertTrue(next.sideEffects.first() is CommentsSideEffects.CouldNotBeLoaded)
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
            assertTrue(next.sideEffects.first() is SaveAsFavoriteSideEffects.Waiting)

            defaultStateMachine.reduce(MovieScreenAction.SideEffectHandled(next.sideEffects.first()))
            next = awaitItem()
            assertEquals(MovieScreenState(), next)

            next = awaitItem()
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is SaveAsFavoriteSideEffects.Succeeded)
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
            assertTrue(next.sideEffects.first() is SaveAsFavoriteSideEffects.Waiting)

            next = awaitItem()
            assertEquals(2, next.sideEffects.size)
            assertTrue(next.sideEffects[1] is SaveAsFavoriteSideEffects.Failed)
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
            assertTrue(next.sideEffects.first() is SaveAsFavoriteSideEffects.Waiting)

            next = awaitItem()
            assertEquals(2, next.sideEffects.size)
            assertTrue(next.sideEffects[1] is SaveAsFavoriteSideEffects.Succeeded)
        }
    }

    private fun defaultStateMachine(
        movie: OrchestratedUpdate<LoadData, AppError, Movie> = OrchestratedUpdate {
            Either.Right(
                Movie(
                    "1",
                    "Movie1",
                ),
            )
        },
        actors: OrchestratedUpdate<LoadData, AppError, Page<Actor>> = OrchestratedUpdate {
            Either.Right(
                Page(
                    available = Available(3),
                    items = listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
            )
        },
        comments: OrchestratedFlowUpdate<LoadComments, AppError, Page<Comment>> = OrchestratedFlowUpdate {
            flowOf(
                Either.Right(
                    Page(
                        available = Available(3),
                        items = listOf(
                            Comment("comment1", "content1"),
                            Comment("comment2", "content2"),
                            Comment("comment3", "content3"),
                        ),
                    ),
                ),
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
