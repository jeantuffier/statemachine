package com.jeantuffier.statemachine.orchestrate

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Orchestration(
    val baseName: String,
    val errorType: KClass<*>,
    val sideEffects: Array<KClass<*>>,
)

enum class LoadingStrategy { SUSPEND, FLOW }

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Orchestrated(
    val trigger: KClass<*>,
    val loadingStrategy: LoadingStrategy,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Action
