package com.jeantuffier.statemachine

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.right
import com.jeantuffier.statemachine.orchestrate.Available
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate
import com.jeantuffier.statemachine.orchestrate.OrchestratedSideEffect
import com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate
import com.jeantuffier.statemachine.orchestrate.Page
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieScreenHelpersTest {

    @Test
    fun loadMovieShouldSucceed() = runTest {
        val input = object : LoadData {
            override val id: String = "1"
            override val offset: Int = 0
            override val limit: Int = 10
        }
        var next = MovieScreenState()
        val movie = Movie("movie1", "movie1")
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Movie> {
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
        val input = object : LoadData {
            override val id: String = "1"
            override val offset: Int = 0
            override val limit: Int = 10
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Movie> {
            Either.Left(AppError.SomeRandomError)
        }
        loadMovieScreenMovie(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertNull(next.movie.value)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            awaitComplete()
        }
    }

    @Test
    fun loadActorsShouldSucceed() = runTest {
        val input = object : LoadData {
            override val id: String = "1"
            override val offset: Int = 0
            override val limit: Int = 10
        }
        var next = MovieScreenState()
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
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Page<Actor>> {
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
        val input = object : LoadData {
            override val id: String = "1"
            override val offset: Int = 0
            override val limit: Int = 10
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Movie> {
            Either.Left(AppError.SomeRandomError)
        }
        loadMovieScreenMovie(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.movie.isLoading)
            assertNull(next.movie.value)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertNull(next.movie.value)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadMovie)

            awaitComplete()
        }
    }

    @Test
    fun loadCommentsShouldSucceed() = runTest {
        val input = object : LoadComments {
            override val id: String = "1"
            override val offset = 0
            override val limit = 3
        }
        var next = MovieScreenState()
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
        val orchestrator = OrchestratedFlowUpdate<LoadComments, AppError, Page<Comment>> {
            flowOf(page1.right(), page2.right())
        }
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
        val input = object : LoadComments {
            override val id: String = "1"
            override val offset = 0
            override val limit = 3
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedFlowUpdate<LoadComments, AppError, Page<Comment>> {
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
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.CouldNotLoadComments)

            awaitComplete()
        }
    }

    @Test
    fun onSaveAsFavoriteShouldSucceed() = runTest {
        val input = object : SaveAsFavorite {
            override val id: String = "1"
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedSideEffect<SaveAsFavorite, AppError> {
            Either.Right(Unit)
        }
        onMovieScreenSaveAsFavorite(input, orchestrator).test {
            next = awaitItem()(next)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            next = onMovieScreenSideEffectHandled(next.sideEffects.first()).first()(next)

            next = awaitItem()(next)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.SaveAsFavoriteSucceeded)

            awaitComplete()
        }
    }

    @Test
    fun onSaveAsFavoriteShouldFail() = runTest {
        val input = object : SaveAsFavorite {
            override val id: String = "1"
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedSideEffect<SaveAsFavorite, AppError> {
            Either.Left(AppError.SomeRandomError)
        }
        onMovieScreenSaveAsFavorite(input, orchestrator).test {
            next = awaitItem()(next)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            next = onMovieScreenSideEffectHandled(next.sideEffects.first()).first()(next)

            next = awaitItem()(next)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.SaveAsFavoriteFailed)

            awaitComplete()
        }
    }

    @Test
    fun handleSideEffectShouldSucceed() = runTest {
        val sideEffect = MovieScreenSideEffects.WaitingForSaveAsFavorite(1)
        var next = MovieScreenState(sideEffects = listOf(sideEffect))
        onMovieScreenSideEffectHandled(sideEffect).test {
            next = awaitItem()(next)
            assertTrue(next.sideEffects.isEmpty())
            awaitComplete()
        }
    }
}
