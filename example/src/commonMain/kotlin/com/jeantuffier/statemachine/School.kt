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
    reducer = { state, event ->
        when (event) {
            is SchoolViewActions.LoadSchoolData -> state.update { dataLoader(state.value, event) }
            is SchoolViewActions.LoadStudentsAction -> state.loadStudents(event, studentLoader)
            is SchoolViewActions.LoadTeachersAction -> state.loadTeachers(event, teacherLoader)
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
