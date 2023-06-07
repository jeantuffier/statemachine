package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration

@Orchestration(
    baseName = "ActorScreen",
    errorType = AppError::class,
    actions = [SaveAsFavorite::class],
)
interface ActorScreenOrchestration {
    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val actor: OrchestratedData<Actor>

    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val movies: OrchestratedPage<Movie>

    @Orchestrated(
        trigger = LoadComments::class,
        loadingStrategy = LoadingStrategy.FLOW,
    )
    val comments: OrchestratedPage<Comment>
}
