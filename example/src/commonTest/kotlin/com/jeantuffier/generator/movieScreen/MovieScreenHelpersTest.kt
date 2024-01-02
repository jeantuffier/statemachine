/*
package com.jeantuffier.statemachine.movieScreen

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.right
import com.jeantuffier.statemachine.Actor
import com.jeantuffier.statemachine.AppError
import com.jeantuffier.statemachine.Comment
import com.jeantuffier.statemachine.ShowCommentsButtonTapped
import com.jeantuffier.statemachine.Movie
import com.jeantuffier.statemachine.OnScreenReady
import com.jeantuffier.statemachine.orchestrate.Available
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieScreenHelpersTest {

    @Test
    fun loadMovieShouldSucceed() = runTest {
        val input = object : OnScreenReady {
            override val id: String = "1"
            override val offset: Offset = Offset(0)
            override val limit: Limit = Limit(10)
        }
        var next = MovieScreenState(isFavorite = false)
        val movie = Movie("movie1", "movie1")
        val orchestrator = OrchestratedUpdate<OnScreenReady, AppError, Movie> {
            Either.Right(movie)
        }
        loadMovieScreenMovie(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(movie, next.movie.value)

            awaitComplete()
        }
    }

    @Test
    fun loadMovieShouldFail() = runTest {
        val input = object : OnScreenReady {
            override val id: String = "1"
            override val offset: Offset = Offset(0)
            override val limit: Limit = Limit(10)
        }
        var next = MovieScreenState(isFavorite = false)
        val orchestrator = OrchestratedUpdate<OnScreenReady, AppError, Movie> {
            Either.Left(AppError.SomeRandomError)
        }
        loadMovieScreenMovie(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertNull(next.movie.value)

            awaitComplete()
        }
    }

    @Test
    fun loadActorsShouldSucceed() = runTest {
        val input = object : OnScreenReady {
            override val id: String = "1"
            override val offset: Offset = Offset(0)
            override val limit: Limit = Limit(10)
        }
        var next = MovieScreenState(isFavorite = false)
        val actors = Page(
            available = Available(3),
            offset = Offset(0),
            limit = Limit(3),
            items = listOf(
                Actor("actor1", "actor1"),
                Actor("actor2", "actor2"),
                Actor("actor3", "actor3"),
            ),
        )
        val orchestrator = OrchestratedUpdate<OnScreenReady, AppError, Page<Actor>> {
            Either.Right(actors)
        }
        loadMovieScreenActors(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.pages.isEmpty())
            assertFalse(next.actors.hasLoadedEverything())

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
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

            awaitComplete()
        }
    }

    @Test
    fun loadActorsShouldFail() = runTest {
        val input = object : OnScreenReady {
            override val id: String = "1"
            override val offset: Offset = Offset(0)
            override val limit: Limit = Limit(10)
        }
        var next = MovieScreenState(isFavorite = false)
        val orchestrator = OrchestratedUpdate<OnScreenReady, AppError, Movie> {
            Either.Left(AppError.SomeRandomError)
        }
        loadMovieScreenMovie(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertNull(next.movie.value)

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldSucceed() = runTest {
        val input = object : ShowCommentsButtonTapped {
            override val id: String = "1"
            override val offset: Offset = Offset(0)
            override val limit: Limit = Limit(3)
        }

        val page1 = Page(
            Available(6),
            Offset(0),
            Limit(3),
            listOf(
                Comment("comment1", "comment1"),
                Comment("comment2", "comment2"),
                Comment("comment3", "comment3"),
            ),
        )
        val page2 = Page(
            Available(6),
            Offset(3),
            Limit(3),
            listOf(
                Comment("comment4", "comment4"),
                Comment("comment5", "comment5"),
                Comment("comment6", "comment6"),
            ),
        )
        val orchestrator = OrchestratedFlowUpdate<ShowCommentsButtonTapped, AppError, Page<Comment>> {
            flow {
                delay(100)
                emit(page1.right())
                delay(100)
                emit(page2.right())
            }
        }

        var next = MovieScreenState(isFavorite = false)
        loadMovieScreenComments(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.comments.isLoading)
            assertEquals(emptyMap(), next.comments.pages)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(1, next.comments.pages.size)
            assertEquals(page1.items, next.comments.pages[0])
            assertFalse(next.comments.hasLoadedEverything())

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(2, next.comments.pages.size)
            assertEquals(page2.items, next.comments.pages[1])
            assertTrue(next.comments.hasLoadedEverything())

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldFail() = runTest {
        val input = object : ShowCommentsButtonTapped {
            override val id: String = "1"
            override val offset: Offset = Offset(0)
            override val limit: Limit = Limit(3)
        }
        var next = MovieScreenState(isFavorite = false)
        val orchestrator = OrchestratedFlowUpdate<ShowCommentsButtonTapped, AppError, Page<Comment>> {
            flowOf(Either.Left(AppError.SomeRandomError))
        }
        loadMovieScreenComments(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.comments.isLoading)
            assertEquals(emptyMap(), next.comments.pages)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(emptyMap(), next.comments.pages)
            assertFalse(next.comments.hasLoadedEverything())

            awaitComplete()
        }
    }
}
*/
