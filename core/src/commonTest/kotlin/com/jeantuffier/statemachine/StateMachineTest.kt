package com.jeantuffier.statemachine

import app.cash.turbine.test
import com.jeantuffier.statemachine.core.Reducer
import com.jeantuffier.statemachine.core.StateMachine
import com.jeantuffier.statemachine.core.StateUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
class StateMachineTest {

    private val testDispatcher: CoroutineContext = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private data class Movie(val id: String, val title: String, val genre: String)
    private data class Actor(val id: String, val firstName: String, val lastName: String)

    private data class MovieScreenState(
        val isLoadingMovie: Boolean = false,
        val movie: Movie? = null,
        val isLoadingActors: Boolean = false,
        val actors: List<Actor> = emptyList(),
    )

    private sealed class MovieScreenStateActions {
        data class LoadMovie(val id: String) : MovieScreenStateActions()
        data class LoadActors(val movieId: String) : MovieScreenStateActions()
        data class LoadComments(val movieId: String) : MovieScreenStateActions()
    }

    private fun movieScreenReducer(
        onLoadMovie: suspend (MovieScreenStateActions.LoadMovie) -> Flow<StateUpdate<MovieScreenState>>,
        onLoadActors: suspend (MovieScreenStateActions.LoadActors) -> Flow<StateUpdate<MovieScreenState>>,
        onLoadComments: suspend (MovieScreenStateActions.LoadComments) -> Flow<StateUpdate<MovieScreenState>>,
    ) = Reducer<MovieScreenStateActions, MovieScreenState> { action ->
        when (action) {
            is MovieScreenStateActions.LoadMovie -> onLoadMovie(action)
            is MovieScreenStateActions.LoadActors -> onLoadActors(action)
            is MovieScreenStateActions.LoadComments -> onLoadComments(action)
        }
    }

    private suspend fun loadMovie(action: MovieScreenStateActions.LoadMovie): Movie {
        delay(100)
        return Movie("1", "Movie1", "Thriller")
    }

    private suspend fun loadActors(action: MovieScreenStateActions.LoadActors): List<Actor> {
        delay(300)
        return listOf(
            Actor("1", "actor1", "actor1"),
            Actor("2", "actor2", "actor2"),
            Actor("3", "actor3", "actor3"),
        )
    }

    enum class Command {
        Command1,
        Command2,
        Command3,
    }

    @Test
    fun concurrency() = testScope.runTest {
        val stateMachine = StateMachine(
            initialValue = 0,
            coroutineContext = testDispatcher,
            reducer = Reducer<Command, Int> { action ->
                when (action) {
                    Command.Command1 -> flowOf(StateUpdate { 1 })
                    Command.Command2 -> {
                        delay(100)
                        flowOf(StateUpdate { 100 })
                    }

                    Command.Command3 -> {
                        delay(500)
                        flowOf(StateUpdate { 500 })
                    }
                }
            },
        )
        val flow: Flow<Int> = stateMachine.state
        flow.test {
            assertEquals(0, awaitItem())

            stateMachine.reduce(Command.Command3)
            stateMachine.reduce(Command.Command2)
            stateMachine.reduce(Command.Command1)

            assertEquals(1, awaitItem())
            assertEquals(100, awaitItem())
            assertEquals(500, awaitItem())
        }
    }

    @Test
    fun shouldEmitInitialValueFirst() = testScope.runTest {
        StateMachine(
            initialValue = MovieScreenState(),
            coroutineContext = testDispatcher,
            reducer = movieScreenReducer(
                onLoadMovie = { _ -> flowOf() },
                onLoadActors = { _ -> flowOf() },
                onLoadComments = { _ -> flowOf() },
            ),
        ).state.test {
            assertEquals(MovieScreenState(), awaitItem())
        }
    }

    @Test
    fun shouldLoadMovie() = testScope.runTest {
        val movieScreen = StateMachine(
            initialValue = MovieScreenState(),
            coroutineContext = testDispatcher,
            reducer = movieScreenReducer(
                onLoadMovie = { action ->
                    flow {
                        emit { it.copy(isLoadingMovie = true) }
                        val movie = loadMovie(action)
                        emit { it.copy(isLoadingMovie = false, movie = movie) }
                    }
                },
                onLoadActors = { _ -> flowOf() },
                onLoadComments = { _ -> flowOf() },
            ),
        )
        movieScreen.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            movieScreen.reduce(MovieScreenStateActions.LoadMovie("1"))

            var next = awaitItem()
            assertTrue(next.isLoadingMovie)
            assertNull(next.movie)

            next = awaitItem()
            assertFalse(next.isLoadingMovie)
            assertEquals(Movie("1", "Movie1", "Thriller"), next.movie)
        }
    }

    @Test
    fun shouldLoadActors() = testScope.runTest {
        val movieScreen = StateMachine(
            initialValue = MovieScreenState(),
            coroutineContext = testDispatcher,
            reducer = movieScreenReducer(
                onLoadMovie = { _ -> flowOf() },
                onLoadActors = { action ->
                    flow {
                        emit { it.copy(isLoadingActors = true) }
                        val actors = loadActors(action)
                        emit { it.copy(isLoadingActors = false, actors = actors) }
                    }
                },
                onLoadComments = { _ -> flowOf() },
            ),
        )
        movieScreen.state.test {
            assertEquals(MovieScreenState(), awaitItem())

            movieScreen.reduce(MovieScreenStateActions.LoadActors("1"))

            var next = awaitItem()
            assertTrue(next.isLoadingActors)
            assertTrue(next.actors.isEmpty())

            next = awaitItem()
            assertFalse(next.isLoadingActors)
            assertEquals(
                listOf(
                    Actor("1", "actor1", "actor1"),
                    Actor("2", "actor2", "actor2"),
                    Actor("3", "actor3", "actor3"),
                ),
                next.actors,
            )
        }
    }
}
