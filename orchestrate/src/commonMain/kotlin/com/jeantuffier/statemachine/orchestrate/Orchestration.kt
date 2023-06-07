package com.jeantuffier.statemachine.orchestrate

import arrow.core.Either
import com.jeantuffier.statemachine.core.StateUpdate
import kotlinx.coroutines.flow.Flow

fun interface OrchestratedUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Either<Error, Type>
}

fun interface OrchestratedFlowUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Flow<Either<Error, Type>>
}

fun interface OrchestratedAction<Input, Output> {
    suspend operator fun invoke(input: Input): Flow<StateUpdate<Output>>
}
