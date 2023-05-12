package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.Action
import com.jeantuffier.statemachine.orchestrate.SideEffectAction

@Action
interface LoadData {
    val id: String
    val offset: Int
    val limit: Int
}

@Action
interface LoadMovie {
    val id: String
}

@Action
interface LoadActors {
    val id: String
    val offset: Int
    val limit: Int
}

@Action
interface LoadComments {
    val id: String
    val offset: Int
    val limit: Int
}

@SideEffectAction
interface SaveAsFavorite {
    val id: String
}
