package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

@ViewState
data class SchoolStaffViewState(
    val staffCount: Int = 0,

    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    val adminEmployees: AsyncData<List<Person>> = AsyncData(emptyList()),
)

class SchoolStaffStateMachine : StateMachine<SchoolStaffViewState, AppEvent> by StateMachineBuilder(
    initialValue = SchoolStaffViewState(),
    reducer = { state, event ->
        val updater = SchoolStaffViewStateUpdater(state)
        when (event) {
            is AppEvent.SchoolStaffEvent.LoadStaffCount -> loadStaffCount()
            is AppEvent.LoadTeachers -> loadTeachers(updater, event)
            is AppEvent.SchoolStaffEvent.LoadAdminEmployees -> loadAdminEmployees()
            else -> {}
        }
    }
)

private fun loadStaffCount() =
    ViewStateTransition<SchoolStaffViewState, AppEvent.SchoolStaffEvent.LoadStaffCount> { state, event ->
        state.update { it.copy(staffCount = 100) }
    }

private fun loadAdminEmployees() =
    ViewStateTransition<SchoolStaffViewState, AppEvent.SchoolStaffEvent.LoadAdminEmployees> { state, event ->
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
