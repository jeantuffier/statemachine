package com.jeantuffier.statemachine

import kotlin.coroutines.CoroutineContext

/**
 * A helper function to create a new state machine.
 * @param initialState: the initial state the machine should take.
 * @param coroutineContext: the context used for creating the state machine supervisor scope to run transitions.
 * @param builder: the lambda for registering transitions in the state machine.
 */
fun <STATE, EVENT, SIDE_EFFECT> createStateMachine(
    initialState: STATE,
    coroutineContext: CoroutineContext,
    builder: StateMachine<STATE, EVENT, SIDE_EFFECT>.() -> Unit
): StateMachine<STATE, EVENT, SIDE_EFFECT> =
    StateMachineImpl<STATE, EVENT, SIDE_EFFECT>(initialState, coroutineContext)
        .apply { builder(this) }

/**
 * An extension function for registering a predefined transition without the need to pass its name.
 * @param transition: the transition to register.
 */
inline fun <STATE, EVENT, reified T : EVENT, SIDE_EFFECT> StateMachine<STATE, EVENT, SIDE_EFFECT>.register(
    transition: Transition<STATE, T>,
) = register(T::class.toString(), transition as Transition<STATE, EVENT>)

/**
 * An extension function for registering a predefined side effect transition without the need to pass its name.
 * @param transition: the transition to register.
 */
inline fun <STATE, EVENT, reified T : EVENT, SIDE_EFFECT> StateMachine<STATE, EVENT, SIDE_EFFECT>.register(
    transition: TransitionWithSideEffect<STATE, T, SIDE_EFFECT>,
) = register(T::class.toString(), transition as TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>)

/**
 * An extension function for registering an anonymous transition without the need to pass its name.
 * @param builder: the lambda constructing the transition, see [transition].
 */
inline fun <STATE, EVENT, reified T : EVENT, SIDE_EFFECT> StateMachine<STATE, EVENT, SIDE_EFFECT>.registerTransition(
    builder: () -> Transition<STATE, T>,
) = register(T::class.toString(), builder() as Transition<STATE, EVENT>)

/**
 * An extension function for registering an anonymous side effect transition without the need to pass its name.
 * @param builder: the lambda constructing the transition, see [transition].
 */
inline fun <STATE, EVENT, reified T : EVENT, SIDE_EFFECT> StateMachine<STATE, EVENT, SIDE_EFFECT>.registerSideEffect(
    builder: () -> TransitionWithSideEffect<STATE, T, SIDE_EFFECT>,
) = register(T::class.toString(), builder() as TransitionWithSideEffect<STATE, EVENT, SIDE_EFFECT>)

/**
 * A helper function returning an anonymous object implementing [Transition].
 * Use this function to avoid boilerplate and achieve a better readability.
 * @param predicate: the lambda used by the created transition for [Transition.isExecutable]
 * @param execution: the lambda used by the created transition for [Transition.execute]
 * @return a [Transition]
 */
fun <STATE, EVENT> transition(
    predicate: IsExecutable<STATE>,
    execution: Execute<EVENT, STATE>,
) = Transition(
    isExecutable = predicate,
    execute = execution
)

/**
 * A helper function returning an anonymous object implementing [TransitionWithSideEffect].
 * Use this function to avoid boilerplate and achieve a better readability.
 * @param predicate: the lambda used by the created transition for [TransitionWithSideEffect.isExecutable]
 * @param execution: the lambda used by the created transition for [TransitionWithSideEffect.execute]
 * @return a [TransitionWithSideEffect]
 */
fun <STATE, EVENT, SIDE_EFFECT> transitionWithSideEffect(
    predicate: IsExecutable<STATE>,
    execution: ExecuteSideEffect<EVENT, SIDE_EFFECT>,
) = TransitionWithSideEffect(
    isExecutable = predicate,
    execute = execution,
)

/**
 * An extension function passing an event as an input for the state machine.
 * @param event: the event acting as an input for the state machine.
 */
inline fun <STATE, EVENT, reified T : EVENT, SIDE_EFFECT> StateMachine<STATE, EVENT, SIDE_EFFECT>.onEvent(
    event: T,
) = onEvent(T::class.toString(), event)
