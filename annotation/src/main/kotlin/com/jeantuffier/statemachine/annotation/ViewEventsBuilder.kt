package com.jeantuffier.statemachine.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewEventsBuilder(val crossViewEvents: Array<KClass<*>>)
