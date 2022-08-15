package com.jeantuffier.statemachine.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class CrossStateProperty(val key: String)
