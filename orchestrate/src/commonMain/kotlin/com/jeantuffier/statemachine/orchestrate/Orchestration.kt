package com.jeantuffier.statemachine.orchestrate

import arrow.core.Either
import com.jeantuffier.statemachine.core.Effect.StateUpdate
import kotlinx.coroutines.flow.Flow

/**
 * When a property annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated] has a loading strategy
 * [com.jeantuffier.statemachine.orchestrate.LoadingStrategy.SUSPEND], the reducer in charge of updating this property
 * will use [OrchestratedUpdate] as the returned type for the lambda containing the logic to update the property.
 */
fun interface OrchestratedUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Either<Error, Type>
}

/**
 * When a property annotated with [com.jeantuffier.statemachine.orchestrate.Orchestrated] has a loading strategy
 * [com.jeantuffier.statemachine.orchestrate.LoadingStrategy.FLOW], the reducer in charge of updating this property
 * will use [OrchestratedFlowUpdate] as the returned type for the lambda containing the logic to update the property.
 */
fun interface OrchestratedFlowUpdate<Input, Error, Type> {
    suspend operator fun invoke(input: Input): Flow<Either<Error, Type>>
}

/**
 * For each class contained in the "actions" property of [com.jeantuffier.statemachine.orchestrate.Orchestration],
 * [OrchestratedAction] is used as the returned type for the lambda containing the logic for that action.
 */
fun interface OrchestratedAction<Input, Output> {
    suspend operator fun invoke(input: Input): Flow<StateUpdate<Output>>
}
