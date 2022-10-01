package com.jeantuffier.statemachine.framework

enum class AsyncDataStatus { INITIAL, LOADING, SUCCESS, ERROR }

data class AsyncData<T>(
    val data: T? = null,
    val status: AsyncDataStatus = AsyncDataStatus.INITIAL,
)

fun <T> AsyncData<T>.status(newStatus: AsyncDataStatus) = copy(status = newStatus)
