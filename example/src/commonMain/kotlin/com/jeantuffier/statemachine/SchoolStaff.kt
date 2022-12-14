package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.StateMachine
import com.jeantuffier.statemachine.framework.StateMachineBuilder
import com.jeantuffier.statemachine.framework.loadAsyncData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : StateMachine<SchoolStaffViewState, SchoolStaffViewEvents> by StateMachineBuilder(
    initialValue = SchoolStaffViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = SchoolStaffViewStateUpdater(state)
        when (event) {
            is SchoolStaffViewEvents.LoadStaffCount -> {
                state.update { it.copy(staffCount = 100) }
            }

            is SchoolStaffViewEvents.LoadTeachersEvent -> launch { updater.loadTeachers(event, teacherLoader) }
            is SchoolStaffViewEvents.LoadAdminEmployees -> launch { loadAdminEmployees(state, event) }
        }
    }
)

private suspend fun loadAdminEmployees(
    state: MutableStateFlow<SchoolStaffViewState>,
    event: SchoolStaffViewEvents.LoadAdminEmployees,
) {
    loadAsyncData(state.value.adminEmployees, event, adminEmployeesLoader).collect { newValue ->
        state.update { it.copy(adminEmployees = newValue) }
    }
}

val adminEmployeesLoader: suspend (SchoolStaffViewEvents.LoadAdminEmployees) -> Either<SomeRandomError, List<Person>> =
    {
        Either.Right(
            listOf(
                Person("admin1", "admin1"),
                Person("admin2", "admin2"),
                Person("admin3", "admin3"),
            )
        )
    }
