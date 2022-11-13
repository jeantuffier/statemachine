package com.jeantuffier.statemachine.framework

import arrow.core.Either
import kotlinx.coroutines.flow.Flow

enum class AsyncDataStatus { INITIAL, LOADING, SUCCESS, ERROR }

data class AsyncData<T>(
    val data: T? = null,
    val status: AsyncDataStatus = AsyncDataStatus.INITIAL,
)

fun <T> AsyncData<T>.status(newStatus: AsyncDataStatus) = copy(status = newStatus)

fun <Key, Event, Error, AsyncDataType> loadAsyncData(
    key: Key,
    loader: suspend (Event) -> Either<Error, AsyncDataType>
): suspend (ViewStateUpdater<Key>, Event) -> Unit = { updater, event ->
    setLoading<Key, AsyncDataType>(key, updater)
    updater.updateValue(
        key = key,
        newValue = newAsyncValue(loader(event)),
    )
}

fun <Key, Event, Error, AsyncDataType> loadAsyncDataFlow(
    key: Key,
    loader: suspend (Event) -> Flow<Either<Error, AsyncDataType>>
): suspend (ViewStateUpdater<Key>, Event) -> Unit = { updater, event ->
    setLoading<Key, AsyncDataType>(key, updater)
    loader(event).collect {
        updater.updateValue(
            key = key,
            newValue = newAsyncValue(it),
        )
    }
}

private fun <Key, AsyncDataType> setLoading(
    key: Key,
    updater: ViewStateUpdater<Key>
) {
    val currentValue = updater.currentValue<AsyncData<AsyncDataType>>(key)
    updater.updateValue(
        key = key,
        newValue = currentValue.copy(status = AsyncDataStatus.LOADING)
    )
}

fun <Error, AsyncDataType> newAsyncValue(
    result: Either<Error, AsyncDataType>
): AsyncData<AsyncDataType> = when (result) {
    is Either.Left -> AsyncData(
        data = null,
        status = AsyncDataStatus.ERROR
    )

    is Either.Right -> AsyncData(
        data = result.value,
        status = AsyncDataStatus.SUCCESS
    )
}


