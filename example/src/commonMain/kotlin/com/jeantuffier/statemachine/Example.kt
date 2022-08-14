package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.ViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.native.concurrent.SharedImmutable

sealed class Event {
    data class IsLoading(val value: Boolean) : Event()

    data class UpdateCounter(val value: Int) : Event()
    object LoadRemoteValue : Event()
}

enum class TransitionKey { IS_LOADING, COUNTER, REMOVE_VALUE }

@ViewState
data class ViewState1(
    val isLoading: Boolean = false,
    val counter: Int = 0,
    val remoteValue: String = "",
)

@ViewState
data class ViewState2(
    val isLoading: Boolean = false,
    val counter: Int = 0,
    val remoteValue: String = "",
    val someOtherSpecificValue: String = "",
)

class ViewState1Updater(
    private val mutableStateFlow: MutableStateFlow<ViewState1>,
) : ViewStateUpdater<TransitionKey> {

    val isLoading: Boolean
        get() = mutableStateFlow.value.isLoading

    val counter: Int
        get() = mutableStateFlow.value.counter

    val remoteValue: String
        get() = mutableStateFlow.value.remoteValue

    fun update(
        isLoading: Boolean = this.isLoading,
        counter: Int = this.counter,
        remoteValue: String = this.remoteValue,
    ) {
        mutableStateFlow.update {
            it.copy(
                isLoading = isLoading,
                counter = counter,
                remoteValue = remoteValue,
            )
        }
    }

    override fun <T> currentValue(key: TransitionKey) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.value.isLoading as T
            TransitionKey.COUNTER -> mutableStateFlow.value.counter as T
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.value.remoteValue as T
        }

    override fun updateValue(key: TransitionKey, newValue: Any) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.update { it.copy(isLoading = newValue as Boolean) }
            TransitionKey.COUNTER -> mutableStateFlow.update { it.copy(counter = newValue as Int) }
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.update { it.copy(remoteValue = newValue as String) }
        }

    override fun updateValues(values: Map<TransitionKey, Any>) =
        values.entries.forEach { updateValue(it.key, it.value) }
}

/* class ViewState2Updater(
    private val mutableStateFlow: MutableStateFlow<ViewState2>,
) : ViewStateUpdater<TransitionKey> {

    override fun <T> currentValue(key: TransitionKey) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.value.isLoading as T
            TransitionKey.COUNTER -> mutableStateFlow.value.counter as T
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.value.remoteValue as T
        }

    override fun updateValue(key: TransitionKey, newValue: Any) =
        when (key) {
            TransitionKey.IS_LOADING -> mutableStateFlow.update { it.copy(isLoading = newValue as Boolean) }
            TransitionKey.COUNTER -> mutableStateFlow.update { it.copy(counter = newValue as Int) }
            TransitionKey.REMOVE_VALUE -> mutableStateFlow.update { it.copy(remoteValue = newValue as String) }
        }

    override fun updateValues(values: Map<TransitionKey, Any>) =
        values.entries.forEach { updateValue(it.key, it.value) }
}*/

@SharedImmutable
val loadingTransition = Transition { updater, event: Event.IsLoading ->
    updater.updateValue(TransitionKey.IS_LOADING, event.value)
}

@SharedImmutable
val updateCounterTransition = Transition { updater, event: Event.UpdateCounter ->
    val counter = updater.currentValue<Int>(TransitionKey.COUNTER)
    if (counter < 5) {
        updater.updateValue(TransitionKey.COUNTER, counter + event.value)
    }
}

@SharedImmutable
val loadRemoteValueTransition = Transition<TransitionKey, Event.LoadRemoteValue> { updater, event ->
    updater.updateValue(TransitionKey.IS_LOADING, true)
    delay(3000)
    updater.updateValues(
        mapOf(
            TransitionKey.IS_LOADING to false,
            TransitionKey.REMOVE_VALUE to "remote value"
        )
    )
}

/*class ViewStateMachine1 : StateMachine<ViewState1, Event> by StateMachineBuilder(
    initialValue = ViewState1(),
    reducer = { state, event ->
        val updater = ViewState1Updater(state)
        when (event) {
            is Event.IsLoading -> loadingTransition(updater, event)
            is Event.UpdateCounter -> updateCounterTransition(updater, event)
            is Event.LoadRemoteValue -> loadRemoteValueTransition(updater, event)
        }
    }
)

class ViewStateMachine2 : StateMachine<ViewState2, Event> by StateMachineBuilder(
    initialValue = ViewState2(),
    reducer = { state, event ->
        val updater = ViewState2Updater(state)
        when (event) {
            is Event.IsLoading -> loadingTransition(updater, event)
            is Event.UpdateCounter -> updateCounterTransition(updater, event)
            is Event.LoadRemoteValue -> loadRemoteValueTransition(updater, event)
        }
    }
)*/
