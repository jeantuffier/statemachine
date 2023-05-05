package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.orchestrate.Action

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

@Action
interface SaveAsFavorite {
    val id: String
}
