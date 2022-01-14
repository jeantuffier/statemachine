package com.jeantuffier.statemachine

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.*
import kotlin.time.ExperimentalTime

data class Customer(
    val id: Int,
    val name: String,
)

data class CustomerScreenState(
    val isLoading: Boolean = false,
    val customers: List<Customer> = emptyList(),
    val error: TestError? = null,
)

sealed class TestError {
    abstract val message: String

    object NetworkError : TestError() {
        override val message = "something went wrong, try again later"
    }
}

sealed class CustomerScreenEvents {
    object ShowLoading : CustomerScreenEvents()
    object HideLoading : CustomerScreenEvents()
    object LoadCustomers : CustomerScreenEvents()
    data class LoadCustomerWithName(val name: String) : CustomerScreenEvents()
    data class CustomerSelected(val id: Int) : CustomerScreenEvents()
    object ShowAboutApp : CustomerScreenEvents()
}

sealed class CustomerScreenSideEffect {
    data class NavigateToCustomerDetails(val id: Int) : CustomerScreenSideEffect()
    object ShowAboutApp : CustomerScreenSideEffect()
}

@ExperimentalTime
@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var builder: StateMachine<CustomerScreenState, CustomerScreenEvents, CustomerScreenSideEffect>.() -> Unit

    private val customers = listOf(
        Customer(id = 0, name = "John"),
        Customer(id = 1, name = "Emma"),
    )

    private val loadCustomerTransition = Transition<CustomerScreenState, CustomerScreenEvents.LoadCustomers>(
        isExecutable = { state -> state.isLoading },
        execute = { _, state ->
            // do some IO operation to load customers
            state.value = state.value.copy(customers = customers)
        }
    )

    private val loadCustomerWithNameTransition =
        Transition<CustomerScreenState, CustomerScreenEvents.LoadCustomerWithName>(
            isExecutable = { state -> state.isLoading },
            execute = { event, state ->
                // do some IO operation to load customers
                state.value = state.value.copy(customers = loadWithFilter(event.name))
            }
        )

    private fun loadWithFilter(name: String): List<Customer> {
        // use the name value from the event to filter the search or whatever.
        return listOf(Customer(id = 0, name = "John"))
    }

    private val customerSelectedTransitionWithSideEffect =
        TransitionWithSideEffect<CustomerScreenState, CustomerScreenEvents.CustomerSelected, CustomerScreenSideEffect>(
            isExecutable = { state -> !state.isLoading },
            execute = { event, sideEffect ->
                sideEffect.emit(CustomerScreenSideEffect.NavigateToCustomerDetails(event.id))
            }
        )

    @BeforeTest
    fun setUp() {
        /**
         * Create and initialize a state machine with a builder lambda.
         */
        builder = {

            /**
             * Register a predefined transition.
             */
            register(loadCustomerTransition)
            register(loadCustomerWithNameTransition)
            register(customerSelectedTransitionWithSideEffect)

            /**
             * Register an anonymous transition here
             */
            registerTransition {
                transition<CustomerScreenState, CustomerScreenEvents.ShowLoading>(
                    predicate = { !it.isLoading },
                    execution = { event, state ->
                        state.value = state.value.copy(isLoading = true)
                    }
                )
            }

            registerTransition {
                transition<CustomerScreenState, CustomerScreenEvents.HideLoading>(
                    predicate = { it.isLoading },
                    execution = { event, state ->
                        state.value = state.value.copy(isLoading = false)
                    }
                )
            }

            registerSideEffect {
                transitionWithSideEffect<CustomerScreenState, CustomerScreenEvents.ShowAboutApp, CustomerScreenSideEffect>(
                    predicate = { !it.isLoading },
                    execution = { event, sideEffect ->
                        sideEffect.emit(CustomerScreenSideEffect.ShowAboutApp)
                    }
                )
            }
        }
    }

    @Test
    fun shouldTransitionToLoadingState() = runBlockingTest {
        val initialState = CustomerScreenState()
        val stateMachine = createStateMachine(initialState, Dispatchers.Unconfined, builder)

        stateMachine.state.test {
            assertEquals(initialState, awaitItem())
            stateMachine.onEvent(CustomerScreenEvents.ShowLoading)

            val nextState = awaitItem()
            assertTrue(nextState.isLoading)
            assertTrue(nextState.customers.isEmpty())
            assertNull(nextState.error)

            cancelAndIgnoreRemainingEvents()
        }

        stateMachine.destroy()
    }

    @Test
    fun shouldTransitionToCustomerLoadedState() = runBlockingTest {
        val initialState = CustomerScreenState(isLoading = true)
        val stateMachine = createStateMachine(initialState, Dispatchers.Unconfined, builder)

        stateMachine.state.test {
            assertEquals(initialState, awaitItem())
            stateMachine.onEvent(CustomerScreenEvents.LoadCustomers)

            val nextState = awaitItem()
            assertTrue(nextState.isLoading)
            assertEquals(customers, nextState.customers)
            assertNull(nextState.error)

            cancelAndIgnoreRemainingEvents()
        }

        stateMachine.destroy()
    }

    @Test
    fun shouldTransitionToFilteredCustomerLoadedState() = runBlockingTest {
        val initialState = CustomerScreenState(isLoading = true)
        val stateMachine = createStateMachine(initialState, Dispatchers.Unconfined, builder)

        stateMachine.state.test {
            assertEquals(initialState, awaitItem())
            stateMachine.onEvent(CustomerScreenEvents.LoadCustomerWithName("John"))

            val nextState = awaitItem()
            assertTrue(nextState.isLoading)
            assertEquals(listOf(Customer(id = 0, name = "John")), nextState.customers)
            assertNull(nextState.error)

            cancelAndIgnoreRemainingEvents()
        }

        stateMachine.destroy()
    }

    @Test
    fun shouldTransitionToHideLoadingState() = runBlockingTest {
        val initialState = CustomerScreenState(isLoading = true, customers = customers)
        val stateMachine = createStateMachine(initialState, Dispatchers.Unconfined, builder)

        stateMachine.state.test {
            assertEquals(initialState, awaitItem())
            stateMachine.onEvent(CustomerScreenEvents.HideLoading)

            val nextState = awaitItem()
            assertFalse(nextState.isLoading)
            assertEquals(customers, nextState.customers)
            assertNull(nextState.error)

            cancelAndIgnoreRemainingEvents()
        }

        stateMachine.destroy()
    }

    @Test
    fun shouldTriggerNavigateToCustomerDetailsSideEffect() = runBlockingTest {
        val initialState = CustomerScreenState()
        val stateMachine = createStateMachine(initialState, Dispatchers.Unconfined, builder)

        stateMachine.state.test {
            assertEquals(initialState, awaitItem())
            assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }

        stateMachine.sideEffects.test {
            stateMachine.onEvent(CustomerScreenEvents.CustomerSelected(1))

            val nextSideEffect = awaitItem()
            assertTrue {
                nextSideEffect is CustomerScreenSideEffect.NavigateToCustomerDetails &&
                        nextSideEffect.id == 1
            }
        }

        stateMachine.destroy()
    }

    @Test
    fun shouldTriggerShowAboutAppSideEffect() = runBlockingTest {
        val initialState = CustomerScreenState()
        val stateMachine = createStateMachine(initialState, Dispatchers.Unconfined, builder)

        stateMachine.state.test {
            assertEquals(initialState, awaitItem())
            assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }

        stateMachine.sideEffects.test {
            stateMachine.onEvent(CustomerScreenEvents.ShowAboutApp)
            assertTrue(awaitItem() is CustomerScreenSideEffect.ShowAboutApp)
        }

        stateMachine.destroy()
    }
}