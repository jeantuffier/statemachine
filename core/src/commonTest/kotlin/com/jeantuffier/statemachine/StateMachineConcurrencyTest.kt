package com.jeantuffier.statemachine

import app.cash.turbine.test
import com.jeantuffier.statemachine.core.Reducer
import com.jeantuffier.statemachine.core.StateMachine
import com.jeantuffier.statemachine.core.StateUpdate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StateMachineConcurrencyTest {

    enum class Command {
        Command1,
        Command2,
        Command3,
    }

    @Test
    fun concurrency() = runTest {
        val stateMachine = StateMachine(
            initialValue = 0,
            coroutineDispatcher = StandardTestDispatcher(testScheduler),
            reducer = Reducer<Command, Int> { action ->
                when (action) {
                    Command.Command1 -> flowOf(StateUpdate { 1 })
                    Command.Command2 -> {
                        delay(100)
                        flowOf(StateUpdate { 100 })
                    }

                    Command.Command3 -> {
                        delay(500)
                        flowOf(StateUpdate { 500 })
                    }
                }
            },
        )
        val flow: Flow<Int> = stateMachine.state
        flow.test {
            assertEquals(0, awaitItem())

            stateMachine.reduce(Command.Command3)
            stateMachine.reduce(Command.Command2)
            stateMachine.reduce(Command.Command1)

            assertEquals(1, awaitItem())
            assertEquals(100, awaitItem())
            assertEquals(500, awaitItem())
        }
    }
}
