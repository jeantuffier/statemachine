# State Machine

A state machine framework for kotlin multi-platform project.

StateMachine let you create and register transitions for a specific state object. Each transition 
requires a predicate checking the current state of the machine to ensure it can be run.

## Gradle
For Kotlin multiplatform, inside `commonMain`
```
implementation "com.jeantuffier:statemachine:$version"
```
For the Jvm and Android
```
implementation "com.jeantuffier:statemachine-jvm:$version"
```
For iOS
```
implementation "com.jeantuffier:statemachine-ios:$version"
```
## Usage 

```
sealed class Event {
    data class Event1(val value: Int) : Event()
}

data class ViewState(val counter: Int = 0)

class Transition1 : Transition<ViewState, Event.Event1> by TransitionBuilder(
    predicate = { it.counter < 5 },
    execution = { state, event ->
        state.copy(counter = state.counter + event.value)
    }
)

class ViewStateMachine(
    private val transition1: Transition1
) : StateMachine<ViewState, Event> by StateMachineBuilder(
    initialValue = ViewState(),
    reducer = {
        when (it) {
            is Event.Event1 -> onEvent(transition1, it)
        }
    }
)

@ExperimentalCoroutinesApi
class StateMachineTest {

    private lateinit var stateMachine: StateMachine<ViewState, Event>

    @BeforeTest
    fun setUp() {
        stateMachine = ViewStateMachine(Transition1())
    }

    @Test
    fun ensureInitialDataIsCorrect() = runBlockingTest {
        stateMachine.state.test {
            assertEquals(ViewState(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun ensurePredicateIsRespected() = runBlockingTest {
        stateMachine.state.test {
            assertEquals(ViewState(), awaitItem())

            stateMachine.reduce(Event.Event1(2))
            assertEquals(2, awaitItem().counter)

            stateMachine.reduce(Event.Event1(3))
            assertEquals(5, awaitItem().counter)

            // nothing should be emitted, if it does, the test should fail with "Unconsumed events found"
            stateMachine.reduce(Event.Event1(2))
        }
    }
}
```
