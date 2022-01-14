# State Machine

A state machine dsl for kotlin multi-platform project.

StateMachine let you create and register transitions for a specific state object. Each transition requires a predicate checking the current state of the machine to ensure it's ran at the correct time.

## Gradle

Inside the `repositories` block from your `build.gradle` file : 
```
maven { url = uri("https://maven.pkg.jetbrains.space/no.beiningbogen/p/stmn/maven") }
```
Inside `dependencies`
```
implementation "no.no.beiningbogen:StateMachine:0.3.0"
```

## Usage 

```
**
 * Create and initialize a state machine with a builder lambda.
 */
val stateMachine: StateMachine<CustomerScreenState, CustomerScreenEvents>
 = createStateMachine(initialState, dispatcher) {
        /**
         * Register a predefined transition.
         */
        register(loadCustomerTransition)
    
        /**
         * Register an anonymous transition here
         */
        register {
            transition<CustomerScreenState, CustomerScreenEvents.ShowLoading>(
                predicate = { !it.isLoading },
                execution = { it.value = it.value.copy(isLoading = true) }
            )
        }
    
        register {
            transition<CustomerScreenState, CustomerScreenEvents.HideLoading>(
                predicate = { it.isLoading },
                execution = { it.value = it.value.copy(isLoading = false) }
            )
        }
    }
...
@Test
fun shouldTransitionToCustomerLoadedState() = dispatcher.runBlockingTest {
    val initialState = CustomerScreenState(isLoading = true)
    stateMachine = createStateMachine(initialState, dispatcher, builder)

    stateMachine.state.test {
        assertEquals(initialState, expectItem())
        stateMachine.onEvent(CustomerScreenEvents.LoadCustomers)

        val nextState = expectItem()
        assertTrue(nextState.isLoading)
        assertEquals(customers, nextState.customers)
        assertNull(nextState.error)

        cancelAndIgnoreRemainingEvents()
    }
}
```

More details in StateMachineTest.kt
