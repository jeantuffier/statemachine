package com.jeantuffier.statemachine

/**
 * INITIAL represents the status of [AsyncData] before fetching its value.
 * LOADING represents the status of [AsyncData] when its value is being fetched.
 * LOADED represents the status of [AsyncData] when its value has been fetched successfully.
 * FAILED represents the status of [AsyncData] when its value couldn't be fetched.
 * FAILED represents the status of [AsyncData] when its value has been fetched successfully but was empty.
 * (to be used with lists)
 */
enum class AsyncDataStatus {
    INITIAL, LOADING, LOADED, FAILED, EMPTY
}

data class AsyncData<T>(
    val status: AsyncDataStatus = AsyncDataStatus.INITIAL,
    val value: T,
)
