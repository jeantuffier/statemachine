package com.jeantuffier.statemachine.framework

import arrow.core.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

enum class AsyncDataStatus { INITIAL, LOADING, SUCCESS, ERROR }

data class AsyncData<T>(
    val data: T? = null,
    val status: AsyncDataStatus = AsyncDataStatus.INITIAL,
)

suspend fun <AsyncDataType, Event, Error> loadAsyncData(
    asyncData: AsyncData<AsyncDataType>,
    event: Event,
    loader: suspend (Event) -> Either<Error, AsyncDataType>,
): Flow<AsyncData<AsyncDataType>> = flow {
    setLoading(asyncData)
    handleLoaderResult(loader(event))
}

suspend fun <AsyncDataType, Event, Error> loadAsyncDataFlow(
    asyncData: AsyncData<AsyncDataType>,
    event: Event,
    loader: suspend (Event) -> Flow<Either<Error, AsyncDataType>>,
): Flow<AsyncData<AsyncDataType>> = flow {
    setLoading(asyncData)
    loader(event).collect(::handleLoaderResult)
}

private suspend fun <AsyncDataType> FlowCollector<AsyncData<AsyncDataType>>.setLoading(
    asyncData: AsyncData<AsyncDataType>
) = emit(asyncData.copy(status = AsyncDataStatus.LOADING))

private suspend fun <AsyncDataType, Error> FlowCollector<AsyncData<AsyncDataType>>.handleLoaderResult(
    result: Either<Error, AsyncDataType>
) = emit(
    when (result) {
        is Either.Left -> AsyncData(
            data = null,
            status = AsyncDataStatus.ERROR
        )

        is Either.Right -> AsyncData(
            data = result.value,
            status = AsyncDataStatus.SUCCESS
        )
    }
)
