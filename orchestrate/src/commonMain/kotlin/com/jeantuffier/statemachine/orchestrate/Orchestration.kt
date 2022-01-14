package com.jeantuffier.statemachine.orchestrate

import arrow.core.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun interface OrchestratedUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Either<Error, Type>
}

fun interface OrchestratedFlowUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Flow<Either<Error, Type>>
}

fun interface OrchestratedSideEffect<Input, Error> {
    suspend operator fun invoke(input: Input): Either<Error, Unit>
}

suspend fun <Type, Input, Error : Throwable> orchestrate(
    input: Input,
    orchestrator: OrchestratedUpdate<Input, Error, Type>,
): Flow<AsyncData<Error, Type>> = flow {
    emit(AsyncData.Loading)
    emit(
        when (val result = orchestrator(input)) {
            is Either.Left -> AsyncData.Failure(result.value)
            is Either.Right -> AsyncData.Success(result.value)
        }
    )
}

suspend fun <Type, Input, Error : Throwable> orchestrate(
    input: Input,
    orchestrator: OrchestratedFlowUpdate<Input, Error, Type>,
): Flow<AsyncData<Error, Type>> = orchestrator(input)
    .map {
        when (it) {
            is Either.Left -> AsyncData.Failure(it.value)
            is Either.Right -> AsyncData.Success(it.value)
        }
    }
    .onStart { emit(AsyncData.Loading) }

suspend fun <Type, Input, Error : Throwable> orchestratePaging(
    input: Input,
    orchestrator: OrchestratedUpdate<Input, Error, Page<Type>>,
): Flow<AsyncData<Error, Page<Type>>> = flow {
    emit(AsyncData.Loading)
    emit(
        when (val result = orchestrator(input)) {
            is Either.Left -> AsyncData.Failure(result.value)
            is Either.Right -> AsyncData.Success(result.value)
        }
    )
}

suspend fun <Type, Input, Error : Throwable> orchestratePaging(
    input: Input,
    orchestrator: OrchestratedFlowUpdate<Input, Error, Page<Type>>,
): Flow<AsyncData<Error, Page<Type>>> = orchestrator(input)
    .map {
        when (it) {
            is Either.Left -> AsyncData.Failure(it.value)
            is Either.Right -> AsyncData.Success(it.value)
        }
    }.onStart { emit(AsyncData.Loading) }

suspend fun <Input, Error : Throwable> orchestrateSideEffect(
    input: Input,
    orchestrator: OrchestratedSideEffect<Input, Error>,
): Flow<AsyncData<Error, Unit>> = flow {
    emit(AsyncData.Loading)
    emit(
        when (val result = orchestrator(input)) {
            is Either.Left -> AsyncData.Failure(result.value)
            is Either.Right -> AsyncData.Success(result.value)
        }
    )
}