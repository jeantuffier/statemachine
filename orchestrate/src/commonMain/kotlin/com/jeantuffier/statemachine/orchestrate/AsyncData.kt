package com.jeantuffier.statemachine.orchestrate

sealed interface AsyncData<out Error, out Data> {
    object Loading : AsyncData<Nothing, Nothing>
    data class Success<T>(val data: T) : AsyncData<Nothing, T>
    data class Failure<T>(val error: T) : AsyncData<T, Nothing>
}
