package com.jeantuffier.statemachine.movieScreen

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.Actor
import com.jeantuffier.statemachine.AppError
import com.jeantuffier.statemachine.Comment
import com.jeantuffier.statemachine.ShowCommentsButtonTapped
import com.jeantuffier.statemachine.Movie
import com.jeantuffier.statemachine.OnScreenReady
import com.jeantuffier.statemachine.SaveAsFavoriteIconTapped
import com.jeantuffier.statemachine.core.StateUpdate
import com.jeantuffier.statemachine.events.CouldNotLoadActors
import com.jeantuffier.statemachine.events.CouldNotLoadComments
import com.jeantuffier.statemachine.events.CouldNotLoadMovie
import com.jeantuffier.statemachine.orchestrate.Available
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.OrchestratedAction
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieScreenTest {

    @Test
    fun shouldBeInitialState() = runTest {
        defaultStateMachine(coroutineDispatcher = StandardTestDispatcher(testScheduler)).state.test {
            val item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            expectNoEvents()
        }
    }

    @Test
    fun shouldLoadData() = runTest {
        val defaultStateMachine = defaultStateMachine(coroutineDispatcher = StandardTestDispatcher(testScheduler))
        defaultStateMachine.state.test {
            var item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            defaultStateMachine.reduce(MovieScreenAction.OnScreenReady("1", Offset(0), Limit(10)))

            item = awaitItem()
            assertTrue(item.movie.isLoading)
            assertNull(item.movie.value)
            assertFalse(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), item.movie.value)
            assertFalse(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), item.movie.value)
            assertTrue(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), item.movie.value)
            assertFalse(item.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                item.actors.pages,
            )
            assertTrue(item.actors.hasLoadedEverything())

            expectNoEvents()
        }
    }

    @Test
    fun shouldFailToLoadMovie() = runTest {
        val defaultStateMachine = defaultStateMachine(
            movie = { _ -> Either.Left(AppError.SomeRandomError) },
            coroutineDispatcher = StandardTestDispatcher(testScheduler),
        )
        defaultStateMachine.state.test {
            var item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            defaultStateMachine.reduce(MovieScreenAction.OnScreenReady("1", Offset(0), Limit(10)))

            item = awaitItem()
            assertTrue(item.movie.isLoading)
            assertNull(item.movie.value)

            item = awaitItem()
            assertFalse(item.movie.isLoading)

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertTrue(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                item.actors.pages,
            )
            assertTrue(item.actors.hasLoadedEverything())

            assertNotNull(item.event)
            assertTrue(item.event is CouldNotLoadMovie)

            expectNoEvents()
        }
    }

    @Test
    fun shouldFailToLoadActors() = runTest {
        val defaultStateMachine = defaultStateMachine(
            actors = { _ -> Either.Left(AppError.SomeRandomError) },
            coroutineDispatcher = StandardTestDispatcher(testScheduler),
        )
        defaultStateMachine.state.test {
            var item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            defaultStateMachine.reduce(MovieScreenAction.OnScreenReady("1", Offset(0), Limit(10)))

            item = awaitItem()
            assertTrue(item.movie.isLoading)
            assertNull(item.movie.value)
            assertFalse(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), item.movie.value)
            assertFalse(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), item.movie.value)
            assertTrue(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.movie.isLoading)
            assertEquals(Movie("1", "Movie1"), item.movie.value)
            assertFalse(item.actors.isLoading)
            assertTrue(item.actors.pages.isEmpty())
            assertFalse(item.actors.hasLoadedEverything())

            assertNotNull(item.event)
            assertTrue(item.event is CouldNotLoadActors)

            expectNoEvents()
        }
    }

    @Test
    fun shouldLoadComments() = runTest {
        val defaultStateMachine = defaultStateMachine(coroutineDispatcher = StandardTestDispatcher(testScheduler))
        defaultStateMachine.state.test {
            var item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            defaultStateMachine.reduce(MovieScreenAction.ShowCommentsButtonTapped("1", Offset(0), Limit(10)))

            item = awaitItem()
            assertTrue(item.comments.isLoading)
            assertTrue(item.comments.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Comment("comment1", "content1"),
                        Comment("comment2", "content2"),
                        Comment("comment3", "content3"),
                    ),
                ),
                item.comments.pages,
            )
            assertTrue(item.comments.hasLoadedEverything())

            expectNoEvents()
        }
    }

    @Test
    fun shouldFailToLoadComments() = runTest {
        val defaultStateMachine = defaultStateMachine(
            comments = { _ -> flowOf(Either.Left(AppError.SomeRandomError)) },
            coroutineDispatcher = StandardTestDispatcher(testScheduler),
        )
        defaultStateMachine.state.test {
            var item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            defaultStateMachine.reduce(MovieScreenAction.ShowCommentsButtonTapped("1", Offset(0), Limit(10)))

            item = awaitItem()
            assertTrue(item.comments.isLoading)
            assertTrue(item.comments.pages.isEmpty())

            item = awaitItem()
            assertFalse(item.comments.isLoading)
            assertFalse(item.comments.hasLoadedEverything())

            assertNotNull(item.event)
            assertTrue(item.event is CouldNotLoadComments)

            expectNoEvents()
        }
    }

    @Test
    fun saveAsFavoriteShouldSucceed() = runTest {
        val defaultStateMachine = defaultStateMachine(coroutineDispatcher = StandardTestDispatcher(testScheduler))
        defaultStateMachine.state.test {
            var item = awaitItem()
            assertFalse(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            defaultStateMachine.reduce(MovieScreenAction.SaveAsFavoriteIconTapped("1"))

            item = awaitItem()
            assertTrue(item.isFavorite)
            assertEquals(OrchestratedData(), item.movie)
            assertEquals(OrchestratedPage(), item.actors)
            assertEquals(OrchestratedPage(), item.comments)

            expectNoEvents()
        }
    }

    private fun defaultStateMachine(
        movie: OrchestratedUpdate<OnScreenReady, AppError, Movie> = OrchestratedUpdate {
            Either.Right(
                Movie(
                    "1",
                    "Movie1",
                ),
            )
        },
        actors: OrchestratedUpdate<OnScreenReady, AppError, Page<Actor>> = OrchestratedUpdate {
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
        comments: OrchestratedFlowUpdate<ShowCommentsButtonTapped, AppError, Page<Comment>> = OrchestratedFlowUpdate {
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
        saveAsFavoriteIconTapped: OrchestratedAction<SaveAsFavoriteIconTapped, MovieScreenState> = OrchestratedAction {
            flowOf(StateUpdate { it.copy(isFavorite = true) })
        },
        coroutineDispatcher: CoroutineDispatcher,
    ) = movieScreenStateMachine(
        movie,
        actors,
        comments,
        saveAsFavoriteIconTapped,
        MovieScreenState(isFavorite = false),
        coroutineDispatcher,
    )
}
