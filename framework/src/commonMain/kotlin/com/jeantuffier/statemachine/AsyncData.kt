package com.jeantuffier.statemachine

enum class AsyncDataStatus { INITIAL, LOADING, SUCCESS, ERROR }

data class AsyncData<T>(
    val data: T?,
    val status: AsyncDataStatus = AsyncDataStatus.INITIAL,
)

fun <T> AsyncData<T>.status(newStatus: AsyncDataStatus) = copy(status = newStatus)

// TODO - generate this
//fun <Key, AsyncDataType, LoadEvent> loadAsyncData(
//    key: Key,
//    loader: (LoadEvent) -> AsyncDataType,
//) = ReusableTransition { updater, event: LoadEvent ->
//    val currentValue = updater.currentValue(key) as AsyncData<AsyncDataType>
//    updater.updateValue(
//        key = key,
//        newValue = currentValue.copy(status = AsyncDataStatus.LOADING)
//    )
//    val data = loader(event)
//    updater.updateValue(
//        key = key,
//        newValue = AsyncData(
//            data = data,
//            status = AsyncDataStatus.SUCCESS
//        )
//    )
//}