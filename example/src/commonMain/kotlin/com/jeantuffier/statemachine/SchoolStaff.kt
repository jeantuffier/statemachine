package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@ViewState
data class SchoolStaffViewState(
    val staffCount: Int = 0,

    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    val adminEmployees: AsyncData<List<Person>> = AsyncData(emptyList()),
)

object LoadStaffCount

data class LoadAdminEmployees(val offset: Int, val limit: Int)

@ViewEventsBuilder(
    crossViewEvents = [
        LoadTeachersEvent::class,
        LoadStaffCount::class,
        LoadAdminEmployees::class,
    ]
)
class SchoolStaffViewEventsBuilder

class SchoolStaffStateMachine(
    private val scope: CoroutineScope = defaultStateMachineScope()
) : StateMachine<SchoolStaffViewState, SchoolStaffViewEvents> by StateMachineBuilder(
    initialValue = SchoolStaffViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = SchoolStaffViewStateUpdater(state)
        when (event) {
            is SchoolStaffViewEvents.LoadStaffCount -> {
                state.update { it.copy(staffCount = 100) }
            }

            is SchoolStaffViewEvents.LoadTeachersEvent -> loadTeachers(updater, event)
            is SchoolStaffViewEvents.LoadAdminEmployees -> loadAdminEmployees(state)
        }
    }
)

private suspend fun loadAdminEmployees(state: MutableStateFlow<SchoolStaffViewState>) {
    val employees = state.value.adminEmployees
    state.update { it.copy(adminEmployees = employees.status(AsyncDataStatus.LOADING)) }
    delay(300)
    state.update {
        it.copy(
            adminEmployees = employees.copy(
                status = AsyncDataStatus.SUCCESS,
                data = listOf(
                    Person("admin1", "admin1"),
                    Person("admin2", "admin2"),
                    Person("admin3", "admin3"),
                )
            ),
        )
    }
}
