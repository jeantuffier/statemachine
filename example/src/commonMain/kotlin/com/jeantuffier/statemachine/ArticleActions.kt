package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.PageLoader

@Action
interface LoadMovies : PageLoader {
    override val offset: Offset
    override val limit: Limit
}

@Action
interface SelectMovie {
    val id: String
}

@Action
interface CloseMovieDetails
