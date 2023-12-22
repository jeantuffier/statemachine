package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration

@Orchestration(
    baseName = "ActorScreen",
    errorType = AppError::class,
    actions = [SaveAsFavoriteIconTapped::class],
)
interface ActorScreenOrchestration {
    @Orchestrated(
        action = OnScreenReady::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val actor: OrchestratedData<Actor>

    @Orchestrated(
        action = OnScreenReady::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val movies: OrchestratedPage<Movie>

    @Orchestrated(
        action = ShowCommentsButtonTapped::class,
        loadingStrategy = LoadingStrategy.FLOW,
    )
    val comments: OrchestratedPage<Comment>
}
