package com.jeantuffier.statemachine.orchestrate

import arrow.core.Either
import kotlinx.coroutines.flow.Flow

fun interface OrchestratedUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Either<Error, Type>
}

fun interface OrchestratedFlowUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Flow<Either<Error, Type>>
}

fun interface OrchestratedSideEffect<Input, Error> {
    suspend operator fun invoke(input: Input): Either<Error, Unit>
}
