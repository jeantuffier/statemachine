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
        }
        var next = MovieScreenState()
        val movie = Movie("movie1", "movie1")
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Movie> {
            Either.Right(movie)
        }
        loadMovie(input, orchestrator).test {
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
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Movie> {
            Either.Left(AppError.SomeRandomError)
        }
        loadMovie(input, orchestrator).test {
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
        }
        var next = MovieScreenState()
        val actors = Page(
            offset = Offset(0),
            limit = Limit(3),
            available = Available(3),
            items = listOf(
                Actor("actor1", "actor1"),
                Actor("actor2", "actor2"),
                Actor("actor3", "actor3"),
            )
        )
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Page<Actor>> {
            Either.Right(actors)
        }
        loadActors(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.actors.isLoading)
            assertTrue(next.actors.items.isEmpty())
            assertFalse(next.actors.hasLoadedEverything())

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(
                listOf(
                    Actor("actor1", "actor1"),
                    Actor("actor2", "actor2"),
                    Actor("actor3", "actor3"),
                ), next.actors.items
            )
            assertTrue(next.actors.hasLoadedEverything())

            awaitComplete()
        }
    }

    @Test
    fun loadActorsShouldFail() = runTest {
        val input = object : LoadData {
            override val id: String = "1"
        }
        var next = MovieScreenState()
        val orchestrator = OrchestratedUpdate<LoadData, AppError, Movie> {
            Either.Left(AppError.SomeRandomError)
        }
        loadMovie(input, orchestrator).test {
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
        val comments = Page(
            Offset(0),
            Limit(3),
            Available(3),
            listOf(
                Comment("comment1", "comment1"),
                Comment("comment2", "comment2"),
                Comment("comment3", "comment3"),
            ),
        )
        val orchestrator = OrchestratedFlowUpdate<LoadComments, AppError, Page<Comment>> {
            flowOf(Either.Right(comments))
        }
        loadComments(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.comments.isLoading)
            assertEquals(emptyList(), next.comments.items)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(comments.items, next.comments.items)
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
        loadComments(input, orchestrator).test {
            next = awaitItem()(next)
            assertTrue(next.comments.isLoading)
            assertEquals(emptyList(), next.comments.items)

            next = awaitItem()(next)
            assertFalse(next.movie.isLoading)
            assertEquals(emptyList(), next.comments.items)
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
        onSaveAsFavorite(input, orchestrator).test {
            next = awaitItem()(next)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            next = onSideEffectHandled(next.sideEffects.first()).first()(next)

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
        onSaveAsFavorite(input, orchestrator).test {
            next = awaitItem()(next)
            assertEquals(1, next.sideEffects.size)
            assertTrue(next.sideEffects.first() is MovieScreenSideEffects.WaitingForSaveAsFavorite)

            next = onSideEffectHandled(next.sideEffects.first()).first()(next)

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
        onSideEffectHandled(sideEffect).test {
            next = awaitItem()(next)
            assertTrue(next.sideEffects.isEmpty())
            awaitComplete()
        }
    }
}