package com.jeantuffier.statemachine.framework

import kotlinx.coroutines.flow.MutableStateFlow

interface Transition

fun interface ViewStateTransition<ViewState, Event> : Transition {
    suspend operator fun invoke(
        state: MutableStateFlow<ViewState>,
        event: Event,
    )
}
