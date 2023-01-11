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

suspend fun <AsyncDataType, Action, Error> loadAsyncData(
    asyncData: AsyncData<AsyncDataType>,
    action: Action,
    loader: suspend (Action) -> Either<Error, AsyncDataType>,
): Flow<AsyncData<AsyncDataType>> = flow {
    setLoading(asyncData)
    handleLoaderResult(asyncData.data, loader(action))
}

suspend fun <AsyncDataType, Action, Error> loadAsyncDataFlow(
    asyncData: AsyncData<AsyncDataType>,
    action: Action,
    loader: suspend (Action) -> Flow<Either<Error, AsyncDataType>>,
): Flow<AsyncData<AsyncDataType>> = flow {
    setLoading(asyncData)
    loader(action).collect {
        handleLoaderResult(asyncData.data, it)
    }
}

private suspend fun <AsyncDataType> FlowCollector<AsyncData<AsyncDataType>>.setLoading(
    asyncData: AsyncData<AsyncDataType>
) = emit(asyncData.copy(status = AsyncDataStatus.LOADING))

private suspend fun <AsyncDataType, Error> FlowCollector<AsyncData<AsyncDataType>>.handleLoaderResult(
    currentValue: AsyncDataType?,
    result: Either<Error, AsyncDataType>
) = emit(
    when (result) {
        is Either.Left -> AsyncData(
            data = currentValue,
            status = AsyncDataStatus.ERROR
        )

        is Either.Right -> AsyncData(
            data = result.value,
            status = AsyncDataStatus.SUCCESS
        )
    }
)
