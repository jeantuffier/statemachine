package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration

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
    val movie: OrchestratedData<Movie>

    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val actors: OrchestratedPage<Actor>

    @Orchestrated(
        trigger = LoadComments::class,
        loadingStrategy = LoadingStrategy.FLOW,
    )
    val comments: OrchestratedPage<Comment>
}
