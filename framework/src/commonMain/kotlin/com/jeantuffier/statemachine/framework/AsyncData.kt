package com.jeantuffier.statemachine.framework

import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

enum class AsyncDataStatus { INITIAL, LOADING, SUCCESS, ERROR }

data class AsyncData<T>(
    val data: T? = null,
    val status: AsyncDataStatus = AsyncDataStatus.INITIAL,
)

suspend fun <AsyncDataType, Event, Error> loadAsyncData(
    asyncData: AsyncData<AsyncDataType>,
    event: Event,
    loader: suspend (Event) -> Either<Error, AsyncDataType>
): Flow<AsyncData<AsyncDataType>> = flow {
    emit(asyncData.copy(status = AsyncDataStatus.LOADING))
    val data: AsyncData<AsyncDataType> = when (val result = loader(event)) {
        is Either.Left -> AsyncData(
            data = null,
            status = AsyncDataStatus.ERROR
        )

        is Either.Right -> AsyncData(
            data = result.value,
            status = AsyncDataStatus.SUCCESS
        )
    }
    emit(data)
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


