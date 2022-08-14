package com.jeantuffier.statemachine.annotation

import kotlin.annotation.AnnotationRetention.SOURCE

@Target(AnnotationTarget.CLASS)
@Retention(SOURCE)
annotation class ViewState
