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

    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
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
    SchoolViewEvents.LoadSchoolData,
) -> SchoolViewState = { state, event -> state.copy(id = "school 1", name = "School One") }

class SchoolStateMachine internal constructor(
    dataLoader: StateUpdate<SchoolViewState, SchoolViewEvents.LoadSchoolData>,
    studentLoader: AsyncDataUpdate<LoadStudentsAction, SomeRandomError, List<Person>>,
    teacherLoader: AsyncDataUpdate<LoadTeachersAction, SomeRandomError, List<Person>>,
    scope: CoroutineScope,
) : StateMachine<SchoolViewState, SchoolViewEvents> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    scope = scope,
    reducer = { state, event ->
        when (event) {
            is SchoolViewEvents.LoadSchoolData -> state.update { dataLoader(state.value, event) }
            is SchoolViewEvents.LoadStudentsEvent -> state.loadStudents(event, studentLoader)
            is SchoolViewEvents.LoadTeachersEvent -> state.loadTeachers(event, teacherLoader)
        }
    }
) {
    companion object {
        fun create(
            loadSchoolData: StateUpdate<SchoolViewState, SchoolViewEvents.LoadSchoolData> = schoolDataLoader,
            loadStudent: AsyncDataUpdate<LoadStudentsAction, SomeRandomError, List<Person>> = studentLoader,
            loadTeacher: AsyncDataUpdate<LoadTeachersAction, SomeRandomError, List<Person>> = teacherLoader,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) = SchoolStateMachine(loadSchoolData, loadStudent, loadTeacher, scope)
    }
}
