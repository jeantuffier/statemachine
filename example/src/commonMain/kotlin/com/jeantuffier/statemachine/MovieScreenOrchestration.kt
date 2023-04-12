package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.Content
import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.Orchestration
import com.jeantuffier.statemachine.orchestrate.PagingContent

@Orchestration(
    baseName = "MovieScreen",
    errorType = AppError::class,
    sideEffects = [SaveAsFavorite::class],
)
interface MovieScreenOrchestration {
    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val movie: Content<Movie>

    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val actors: PagingContent<Actor>

    @Orchestrated(
        trigger = LoadComments::class,
        loadingStrategy = LoadingStrategy.FLOW,
    )
    val comments: PagingContent<Comment>
}
