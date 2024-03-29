package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.Limit
import com.jeantuffier.statemachine.orchestrate.Offset
import com.jeantuffier.statemachine.orchestrate.PageLoader

@Action
interface LoadData : PageLoader {
    val id: String
    override val offset: Offset
    override val limit: Limit
}

@Action
interface LoadMovie {
    val id: String
}

@Action
interface LoadComments : PageLoader {
    val id: String
    override val offset: Offset
    override val limit: Limit
}

@Action
interface SaveAsFavorite {
    val id: String
}
