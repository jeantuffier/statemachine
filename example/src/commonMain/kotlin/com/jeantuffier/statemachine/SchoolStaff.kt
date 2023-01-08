package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewActionsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.loadAsyncData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@ViewState
data class SchoolStaffViewState(
    val staffCount: Int = 0,

    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    val adminEmployees: AsyncData<List<Person>> = AsyncData(emptyList()),
)

@ViewActionsBuilder(
    className = "SchoolStaffViewActions",
    crossActions = [LoadTeachersAction::class],
)
sealed class SchoolStaffViewActionsBuilder {
    object LoadStaffCount : SchoolStaffViewActionsBuilder()
    class LoadAdminEmployees(val offset: Int, val limit: Int) : SchoolStaffViewActionsBuilder()
}

private val staffCountLoader: (
    MutableStateFlow<SchoolStaffViewState>,
    SchoolStaffViewEvents.LoadStaffCount,
) -> Unit = { state, event ->
    state.update {
        it.copy(staffCount = 100)
    }
}

private val adminEmployeesLoader: suspend (
    state: MutableStateFlow<SchoolStaffViewState>,
    event: SchoolStaffViewEvents.LoadAdminEmployees,
) -> Unit = { state, event ->
    loadAsyncData(state.value.adminEmployees, event) {
        Either.Right(
            listOf(
                Person("admin1", "admin1"),
                Person("admin2", "admin2"),
                Person("admin3", "admin3"),
            )
        )
    }.collect { newValue ->
        state.update { it.copy(adminEmployees = newValue) }
    }
}

/*class SchoolStaffStateMachine(
    loadStaffCount: (MutableStateFlow<SchoolStaffViewState>, SchoolStaffViewEvents.LoadStaffCount) -> Unit = staffCountLoader,
    loadAdminEmployees: suspend (MutableStateFlow<SchoolStaffViewState>, SchoolStaffViewEvents.LoadAdminEmployees) -> Unit = adminEmployeesLoader,
    loadTeachers: suspend (LoadTeachersEvent) -> Either<SomeRandomError, List<Person>> = teacherLoader,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : StateMachine<SchoolStaffViewState, SchoolStaffViewEvents> by StateMachineBuilder(
    initialValue = SchoolStaffViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = SchoolStaffViewStateUpdater(state)
        when (event) {
            is SchoolStaffViewEvents.LoadStaffCount -> loadStaffCount(state, event)
            is SchoolStaffViewEvents.LoadAdminEmployees -> scope.launch { loadAdminEmployees(state, event) }
            is SchoolStaffViewEvents.LoadTeachersEvent -> scope.launch { updater.loadTeachers(event, loadTeachers) }
        }
    }
)*/
