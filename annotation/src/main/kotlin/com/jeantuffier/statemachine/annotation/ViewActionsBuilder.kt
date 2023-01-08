package com.jeantuffier.statemachine.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewActionsBuilder(
    val className: String,
    val crossActions: Array<KClass<*>>,
)
