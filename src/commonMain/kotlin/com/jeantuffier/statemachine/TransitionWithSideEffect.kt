package com.jeantuffier.statemachine

data class TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>(
    /**
     * A lambda used to determine if the transition should be executed or not.
     * It has a state parameter as input, it should be the current state of the
     * state machine. A typical use case would be to check if data has already
     * been loaded before executing the transition if its purpose is to load data.
     */
    val isExecutable: IsExecutable<STATE>,

    /**
     * A lambda executing the logic to trigger the desired side effect.
     */
    val execute: ExecuteSideEffect<EVENT, SIDE_EFFECT>,
)
