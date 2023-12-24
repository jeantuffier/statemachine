package com.jeantuffier.statemachine.orchestrate

import kotlin.reflect.KClass

/**
 * A class annotated with [Orchestration] will make KSP generate the following :
 * - a sealed class containing the possible user inputs.
 * - a class representing the screen state.
 * - an implementation of [com.jeantuffier.statemachine.core.Reducer]
 * - an implementation of [com.jeantuffier.statemachine.core.StateMachine]
 * - a file containing helper functions used by the generated reducer.
 *
 * @param baseName Each generated file will use this value as prefix for its name.
 *
 * @param errorType The error type used by the app.
 * In order to work properly, the annotation expects a type used that should be used across the app for logic errors.
 * This can either be [Throwable], [Exception] or any custom type like a sealed class listing all the possible error.
 *
 * @param actions A list of actions not associated with any state value.
 * It is possible to add actions to the generated reducer that are not associated with a state value, in opposition to
 * actions used in [Orchestrated] annotation. The [com.jeantuffier.statemachine.core.Reducer] will use the type
 * [OrchestratedAction] for the lambda associated with the input.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Orchestration(
    val baseName: String,
    val errorType: KClass<*>,
    val actions: Array<KClass<*>>,
)

/**
 * A property annotated with [Orchestrated] needs to declare how its content is loaded :
 * - if it's loaded through a suspend function returning a single value, [SUSPEND] should be used.
 * - if the value is loaded and updated through a flow, [FLOW] should be used
 */
enum class LoadingStrategy { SUSPEND, FLOW }

/**
 * Annotate a property with [Orchestrated] when it should be used in the generated state class.
 * @param action Is the class used by the generated [com.jeantuffier.statemachine.core.Reducer] as the input for
 * loading the data and update the annotated property. That class has to be annotated with [Action]
 * @param loadingStrategy The strategy to use to load the data associated with the property.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Orchestrated(
    val action: KClass<*>,
    val loadingStrategy: LoadingStrategy,
)

/**
 * Tell ksp to use this class as input in when generating a [com.jeantuffier.statemachine.core.Reducer]
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Action


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class UseCase(
    val action: KClass<*>,
    val useCase: KClass<*>,
)
