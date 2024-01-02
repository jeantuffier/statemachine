/*
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
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieScreenReducerTest {

    @Test
    fun loadDataShouldSucceed() = runTest {
        val reducer = createReducer()
        val input = MovieScreenAction.OnScreenReady("1", Offset(0), Limit(10))
        var state = MovieScreenState(isFavorite = false)
        reducer(input).test {
            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertTrue(state.movie.isLoading)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertEquals(state.movie.value, Movie(id = "1", title = "Movie1"))

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertEquals(state.movie.value, Movie(id = "1", title = "Movie1"))
            assertEquals(0, state.actors.available.value)
            assertTrue(state.actors.isLoading)
            assertEquals(emptyMap(), state.actors.pages)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertEquals(state.movie.value, Movie(id = "1", title = "Movie1"))
            assertEquals(3, state.actors.available.value)
            assertFalse(state.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                state.actors.pages,
            )

            awaitComplete()
        }
    }

    @Test
    fun loadMovieShouldFail() = runTest {
        val reducer = createReducer(
            movie = { Either.Left(AppError.SomeRandomError) },
        )
        val input = MovieScreenAction.OnScreenReady("1", Offset(0), Limit(10))
        var state = MovieScreenState(isFavorite = false)
        reducer(input).test {
            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertTrue(state.movie.isLoading)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertNull(state.movie.value)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertNull(state.movie.value)
            assertEquals(0, state.actors.available.value)
            assertTrue(state.actors.isLoading)
            assertEquals(emptyMap(), state.actors.pages)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertNull(state.movie.value)
            assertEquals(3, state.actors.available.value)
            assertFalse(state.actors.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                state.actors.pages,
            )

            assertNotNull(state.event)
            assertTrue(state.event is CouldNotLoadMovie)

            awaitComplete()
        }
    }

    @Test
    fun loadActorsShouldFail() = runTest {
        val reducer = createReducer(
            actors = { Either.Left(AppError.SomeRandomError) },
        )
        val input = MovieScreenAction.OnScreenReady("1", Offset(0), Limit(10))
        var state = MovieScreenState(isFavorite = false)
        reducer(input).test {
            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertTrue(state.movie.isLoading)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertEquals(Movie(id = "1", title = "Movie1"), state.movie.value)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertEquals(Movie(id = "1", title = "Movie1"), state.movie.value)
            assertEquals(0, state.actors.available.value)
            assertTrue(state.actors.isLoading)
            assertEquals(emptyMap(), state.actors.pages)

            state = awaitItem()(state)
            assertFalse(state.isFavorite)
            assertFalse(state.movie.isLoading)
            assertEquals(Movie(id = "1", title = "Movie1"), state.movie.value)
            assertEquals(0, state.actors.available.value)
            assertFalse(state.actors.isLoading)
            assertEquals(emptyMap(), state.actors.pages)

            assertNotNull(state.event)
            assertTrue(state.event is CouldNotLoadActors)

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldSucceed() = runTest {
        val reducer = createReducer()
        val input = MovieScreenAction.OnScreenReady("1", Offset(0), Limit(3))
        var state = MovieScreenState(isFavorite = false)
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(0, state.comments.available.value)
            assertTrue(state.comments.isLoading)
            assertEquals(emptyMap(), state.comments.pages)

            state = awaitItem()(state)
            assertEquals(3, state.comments.available.value)
            assertFalse(state.comments.isLoading)
            assertEquals(
                mapOf(
                    0 to listOf(
                        Comment("comment1", "content1"),
                        Comment("comment2", "content2"),
                        Comment("comment3", "content3"),
                    ),
                ),
                state.comments.pages,
            )

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldFail() = runTest {
        val reducer = createReducer(
            comments = { flowOf(Either.Left(AppError.SomeRandomError)) },
        )
        val input = MovieScreenAction.OnScreenReady("1", Offset(0), Limit(3))
        var state = MovieScreenState(isFavorite = false)
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(0, state.comments.available.value)
            assertTrue(state.comments.isLoading)
            assertEquals(emptyMap(), state.comments.pages)

            state = awaitItem()(state)
            assertEquals(0, state.comments.available.value)
            assertFalse(state.comments.isLoading)
            assertEquals(emptyMap(), state.comments.pages)

            assertNotNull(state.event)
            assertTrue(state.event is CouldNotLoadComments)

            awaitComplete()
        }
    }

    @Test
    fun saveAsFavoriteShouldSucceed() = runTest {
        val reducer = createReducer()
        val input = MovieScreenAction.SaveAsFavoriteIconTapped("1")
        var state = MovieScreenState(isFavorite = false)
        reducer(input).test {
            state = awaitItem()(state)
            assertTrue(state.isFavorite)

            awaitComplete()
        }
    }

    private fun createReducer(
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
    ) = movieScreenReducer(
        movie,
        actors,
        comments,
        saveAsFavoriteIconTapped,
    )
}
*/
