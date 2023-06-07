package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.LoadingStrategy
import com.jeantuffier.statemachine.orchestrate.Orchestrated
import com.jeantuffier.statemachine.orchestrate.OrchestratedData
import com.jeantuffier.statemachine.orchestrate.OrchestratedPage
import com.jeantuffier.statemachine.orchestrate.Orchestration

@Orchestration(
    baseName = "ArticleScreen",
    errorType = AppError::class,
    actions = [CloseMovieDetails::class],
)
interface ArticleScreenOrchestration {
    @Orchestrated(
        trigger = LoadMovies::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val movies: OrchestratedPage<Movie>

    @Orchestrated(
        trigger = SelectMovie::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val movie: OrchestratedData<Movie>

    val someRandomValue: String
}
