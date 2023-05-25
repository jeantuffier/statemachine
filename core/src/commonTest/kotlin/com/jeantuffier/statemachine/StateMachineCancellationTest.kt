package com.jeantuffier.statemachine

import app.cash.turbine.test
import com.jeantuffier.statemachine.core.StateMachine
import com.jeantuffier.statemachine.core.StateUpdate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

data class MovieOverview(val id: String, val name: String)

data class Movie(val id: String, val name: String, val poster: String, val actors: List<String>)

data class MovieScreenState(
    val isLoading: Boolean = false,
    val movies: List<MovieOverview> = emptyList(),
    val selectedMovie: Movie? = null,
)

sealed class MovieScreenAction {
    object LoadMovies : MovieScreenAction()
    class SelectMovie(val id: String) : MovieScreenAction()
    object CloseMovieDetails : MovieScreenAction()
}

class StateMachineCancellationTest {

    private val movies = listOf(
        MovieOverview("1", "movie1"),
        MovieOverview("2", "movie2"),
        MovieOverview("3", "movie3"),
    )

    private val movie = Movie("1", "movie1", "path/to/poster/1.jpg", listOf("actor1", "actor2"))

    @Test
    fun loadMovies() = runTest {
        val stateMachine = movieScreenStateMachine(StandardTestDispatcher(testScheduler))
        stateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            stateMachine.reduce(MovieScreenAction.LoadMovies)
            assertTrue(awaitItem().isLoading)

            val item = awaitItem()
            assertFalse(item.isLoading)
            assertEquals(
                expected = listOf(
                    MovieOverview("1", "movie1"),
                    MovieOverview("2", "movie2"),
                    MovieOverview("3", "movie3"),
                ),
                actual = item.movies,
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun loadMovieDetailsWithoutCancelling() = runTest {
        val stateMachine = movieScreenStateMachine(StandardTestDispatcher(testScheduler))
        stateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            stateMachine.reduce(MovieScreenAction.LoadMovies)
            assertTrue(awaitItem().isLoading)

            var next = awaitItem()
            assertFalse(next.isLoading)
            assertEquals(movies, next.movies)

            stateMachine.reduce(MovieScreenAction.SelectMovie("1"))
            stateMachine.reduce(MovieScreenAction.CloseMovieDetails)

            // update from MovieScreenAction.SelectMovie
            next = awaitItem()
            assertEquals(
                expected = MovieScreenState(
                    isLoading = true,
                    movies = movies,
                    selectedMovie = null,
                ),
                actual = next,
            )

            // update from MovieScreenAction.CloseMovieDetails
            next = awaitItem()
            assertEquals(
                expected = MovieScreenState(
                    isLoading = false,
                    movies = movies,
                    selectedMovie = null,
                ),
                actual = next,
            )

            // update from MovieScreenAction.SelectMovie
            next = awaitItem()
            assertEquals(
                expected = MovieScreenState(
                    isLoading = false,
                    movies = movies,
                    selectedMovie = movie,
                ),
                actual = next,
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun cancelLoadMovieDetails() = runTest {
        val stateMachine = movieScreenStateMachine(StandardTestDispatcher(testScheduler))
        stateMachine.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            stateMachine.reduce(MovieScreenAction.LoadMovies)
            assertTrue(awaitItem().isLoading)

            val next = awaitItem()
            assertFalse(next.isLoading)
            assertEquals(movies, next.movies)

            stateMachine.reduce(MovieScreenAction.SelectMovie("1"))
            stateMachine.cancel(MovieScreenAction.SelectMovie("1")) {
                it.copy(isLoading = false, selectedMovie = null)
            }

            ensureAllEventsConsumed()
        }
    }

    private fun movieScreenStateMachine(coroutineDispatcher: CoroutineDispatcher) =
        StateMachine<MovieScreenAction, MovieScreenState>(
            initialValue = MovieScreenState(),
            coroutineDispatcher = coroutineDispatcher,
            reducer = {
                when (it) {
                    is MovieScreenAction.LoadMovies -> onLoadMovies()
                    is MovieScreenAction.SelectMovie -> onSelectMovie(it.id)
                    is MovieScreenAction.CloseMovieDetails -> onSelectMovie(null)
                }
            },
        )

    private suspend fun onLoadMovies(): Flow<StateUpdate<MovieScreenState>> = flow {
        emit { it.copy(isLoading = true) }
        delay(2000) // pretend it takes a while to load our movies
        emit { it.copy(isLoading = false, movies = movies) }
    }

    private suspend fun onSelectMovie(id: String?): Flow<StateUpdate<MovieScreenState>> =
        flow {
            val movie = if (id == null) {
                null
            } else {
                emit { it.copy(isLoading = true) }
                delay(500) // pretend it takes a while to load a single movie
                movie
            }
            emit { it.copy(isLoading = false, selectedMovie = movie) }
        }
}
