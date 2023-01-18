package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewActionsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update

@ViewState
data class SchoolViewState(
    val id: String = "",
    val name: String = "",

    @CrossStateProperty
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    @CrossStateProperty
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),

    @CrossStateProperty
    val uiEvents: List<UiEvent> = emptyList()
)

@ViewActionsBuilder(
    className = "SchoolViewActions",
    crossActions = [
        LoadStudentsAction::class,
        LoadTeachersAction::class,
    ]
)
sealed class SchoolViewActionsBuilder {
    object LoadSchoolData : SchoolViewActionsBuilder()
    object ClickedOnStudent : SchoolViewActionsBuilder()
    object ClickedOnTeacher : SchoolViewActionsBuilder()
}

sealed class SchoolViewEvents : UiEvent {
    class NavigateToStudents(override val id: String) : SchoolViewEvents()
    class NavigateToStaff(override val id: String) : SchoolViewEvents()
}

private val schoolDataLoader: (
    SchoolViewState,
    SchoolViewActions.LoadSchoolData,
) -> SchoolViewState = { state, event -> state.copy(id = "school 1", name = "School One") }

class SchoolStateMachine internal constructor(
    dataLoader: StateUpdate<SchoolViewState, SchoolViewActions.LoadSchoolData>,
    studentLoader: AsyncDataUpdate<LoadStudentsAction, SomeRandomError, List<Person>>,
    teacherLoader: AsyncDataUpdate<LoadTeachersAction, SomeRandomError, List<Person>>,
    scope: CoroutineScope,
) : StateMachine<SchoolViewState, SchoolViewActions> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    scope = scope,
    reducer = { state, action ->
        when (action) {
            is SchoolViewActions.LoadSchoolData -> state.update { dataLoader(state.value, action) }
            is SchoolViewActions.LoadStudentsAction -> state.loadStudents(action, studentLoader)
            is SchoolViewActions.LoadTeachersAction -> state.loadTeachers(action, teacherLoader)
            is SchoolViewActions.ClickedOnStudent -> state.onUiEvents(action, ::uiEvenFactory)
            is SchoolViewActions.ClickedOnTeacher -> state.onUiEvents(action, ::uiEvenFactory)
        }
    }
) {
    companion object {
        fun create(
            loadSchoolData: StateUpdate<SchoolViewState, SchoolViewActions.LoadSchoolData> = schoolDataLoader,
            loadStudent: AsyncDataUpdate<LoadStudentsAction, SomeRandomError, List<Person>> = studentLoader,
            loadTeacher: AsyncDataUpdate<LoadTeachersAction, SomeRandomError, List<Person>> = teacherLoader,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) = SchoolStateMachine(loadSchoolData, loadStudent, loadTeacher, scope)
    }
}

private fun uiEvenFactory(action: SchoolViewActions) = when(action) {
    is SchoolViewActions.ClickedOnStudent -> SchoolViewEvents.NavigateToStudents("1")
    is SchoolViewActions.ClickedOnTeacher -> SchoolViewEvents.NavigateToStaff("2")
    else -> null
}
