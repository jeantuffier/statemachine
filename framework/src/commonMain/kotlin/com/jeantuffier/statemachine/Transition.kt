package com.jeantuffier.statemachine

import kotlinx.coroutines.flow.MutableStateFlow

sealed interface Transition

fun interface ViewStateTransition<ViewState, Event>: Transition {
    suspend operator fun invoke(
        state: MutableStateFlow<ViewState>,
        event: Event,
    )
}

fun interface ReusableTransition<Key, Event>: Transition {
    suspend operator fun invoke(
        updater: ViewStateUpdater<Key>,
        event: Event,
    )
}
