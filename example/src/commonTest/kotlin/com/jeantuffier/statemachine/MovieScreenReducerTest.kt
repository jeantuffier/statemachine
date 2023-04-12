package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import com.jeantuffier.statemachine.orchestrate.Available
import com.jeantuffier.statemachine.orchestrate.Content
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedSideEffect
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import com.jeantuffier.statemachine.orchestrate.PagingContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieScreenReducerTest {

    @Test
    fun loadDataShouldSucceed() = runTest {
        val reducer = createReducer()
        val input = MovieScreenAction.LoadData("1")
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(
                MovieScreenState(movie = Content(isLoading = true)),
                state,
            )

            state = awaitItem()(state)
            assertEquals(
                MovieScreenState(
                    movie = Content(
                        isLoading = false,
                        value = Movie(id = "1", title = "Movie1"),
                    ),
                ),
                state,
            )

            state = awaitItem()(state)
            assertEquals(
                MovieScreenState(
                    movie = Content(
                        isLoading = false,
                        value = Movie(id = "1", title = "Movie1"),
                    ),
                    actors = PagingContent(
                        available = Available(0),
                        isLoading = true,
                        items = emptyList(),
                    ),
                ),
                state,
            )

            state = awaitItem()(state)
            assertEquals(
                MovieScreenState(
                    movie = Content(
                        isLoading = false,
                        value = Movie(id = "1", title = "Movie1"),
                    ),
                    actors = PagingContent(
                        available = Available(3),
                        isLoading = false,
                        items = listOf(
                            Actor("actor1", "actor1"),
                            Actor("actor2", "actor2"),
                            Actor("actor3", "actor3"),
                        ),
                    ),
                ),
                state,
            )

            awaitComplete()
        }
    }

    @Test
    fun loadMovieShouldFail() = runTest {
        val reducer = createReducer(
            movie = { Either.Left(AppError.SomeRandomError) },
        )
        val input = MovieScreenAction.LoadData("1")
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(
                MovieScreenState(movie = Content(isLoading = true)),
                state,
            )

            state = awaitItem()(state)
            assertEquals(
                Content<Movie>(
                    isLoading = false,
                    value = null,
                ),
                state.movie,
            )
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            state = awaitItem()(state)
            assertEquals(
                Content<Movie>(
                    isLoading = false,
                    value = null,
                ),
                state.movie,
            )
            assertEquals(
                PagingContent(
                    available = Available(0),
                    isLoading = true,
                    items = emptyList(),
                ),
                state.actors,
            )
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            state = awaitItem()(state)
            assertEquals(
                Content<Movie>(
                    isLoading = false,
                    value = null,
                ),
                state.movie,
            )
            assertEquals(
                PagingContent(
                    available = Available(3),
                    isLoading = false,
                    items = listOf(
                        Actor("actor1", "actor1"),
                        Actor("actor2", "actor2"),
                        Actor("actor3", "actor3"),
                    ),
                ),
                state.actors,
            )
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            awaitComplete()
        }
    }

    @Test
    fun loadActorsShouldFail() = runTest {
        val reducer = createReducer(
            actors = { Either.Left(AppError.SomeRandomError) },
        )
        val input = MovieScreenAction.LoadData("1")
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(
                MovieScreenState(movie = Content(isLoading = true)),
                state,
            )

            state = awaitItem()(state)
            assertEquals(
                Content(
                    isLoading = false,
                    value = Movie(id = "1", title = "Movie1"),
                ),
                state.movie,
            )

            state = awaitItem()(state)
            assertEquals(
                Content(
                    isLoading = false,
                    value = Movie(id = "1", title = "Movie1"),
                ),
                state.movie,
            )
            assertEquals(
                PagingContent(
                    available = Available(0),
                    isLoading = true,
                    items = emptyList(),
                ),
                state.actors,
            )

            state = awaitItem()(state)
            assertEquals(
                Content(
                    isLoading = false,
                    value = Movie(id = "1", title = "Movie1"),
                ),
                state.movie,
            )
            assertEquals(
                PagingContent(
                    available = Available(0),
                    isLoading = false,
                    items = emptyList(),
                ),
                state.actors,
            )
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadActors)

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldSucceed() = runTest {
        val reducer = createReducer()
        val input = MovieScreenAction.LoadComments("1", 0, 3)
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(
                PagingContent(
                    available = Available(0),
                    isLoading = true,
                    items = emptyList(),
                ),
                state.comments,
            )

            state = awaitItem()(state)
            assertEquals(
                PagingContent(
                    available = Available(3),
                    isLoading = false,
                    items = listOf(
                        Comment("comment1", "content1"),
                        Comment("comment2", "content2"),
                        Comment("comment3", "content3"),
                    ),
                ),
                state.comments,
            )

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldFail() = runTest {
        val reducer = createReducer(
            comments = { flowOf(Either.Left(AppError.SomeRandomError)) },
        )
        val input = MovieScreenAction.LoadComments("1", 0, 3)
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(
                PagingContent(
                    available = Available(0),
                    isLoading = true,
                    items = emptyList(),
                ),
                state.comments,
            )

            state = awaitItem()(state)
            assertEquals(
                PagingContent(
                    available = Available(0),
                    isLoading = false,
                    items = emptyList(),
                ),
                state.comments,
            )
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadComments)

            awaitComplete()
        }
    }

    @Test
    fun saveAsFavoriteShouldSucceed() = runTest {
        val reducer = createReducer()
        val input = MovieScreenAction.SaveAsFavorite("1")
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            state = awaitItem()(state)
            assertEquals(2, state.sideEffects.size)
            assertTrue(state.sideEffects[1] is MovieScreenSideEffects.SaveAsFavoriteSucceeded)

            awaitComplete()
        }
    }

    @Test
    fun saveAsFavoriteShouldFail() = runTest {
        val reducer = createReducer(
            saveAsFavorite = { Either.Left(AppError.SomeRandomError) },
        )
        val input = MovieScreenAction.SaveAsFavorite("1")
        var state = MovieScreenState()
        reducer(input).test {
            state = awaitItem()(state)
            assertEquals(1, state.sideEffects.size)
            assertTrue(state.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            state = awaitItem()(state)
            assertEquals(2, state.sideEffects.size)
            assertTrue(state.sideEffects[1] is MovieScreenSideEffects.SaveAsFavoriteFailed)

            awaitComplete()
        }
    }

    @Test
    fun sideEffectHandledShouldSucceed() = runTest {
        val reducer = createReducer()
        val sideEffect = MovieScreenSideEffects.WaitingForSaveAsFavorite(1)
        val input = MovieScreenAction.SideEffectHandled(sideEffect)
        var state = MovieScreenState(
            sideEffects = listOf(MovieScreenSideEffects.WaitingForSaveAsFavorite(1)),
        )
        reducer(input).test {
            state = awaitItem()(state)
            assertTrue(state.sideEffects.isEmpty())
            awaitComplete()
        }
    }

    private fun createReducer(
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
                    offset = Offset(0),
                    limit = Limit(0),
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
                        offset = Offset(0),
                        limit = Limit(0),
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
            Either.Right(Unit)
        },
    ) = movieScreenReducer(
        movie,
        actors,
        comments,
        saveAsFavorite,
    )
}
