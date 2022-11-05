package com.jeantuffier.statemachine.framework

interface ViewStateUpdater<Key> {
    fun <T> currentValue(key: Key): T

    fun <T> updateValue(key: Key, newValue: T)

    fun <T> updateValues(values: Map<Key, T>)
}