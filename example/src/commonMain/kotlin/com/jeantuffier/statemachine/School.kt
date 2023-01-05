package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
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

@ViewEventsBuilder(
    crossViewEvents = [
        LoadStudentsEvent::class,
        LoadTeachersEvent::class,
    ]
)
sealed class SchoolViewEventsBuilder {
    object LoadSchoolData : SchoolViewEventsBuilder()
}

private val schoolDataLoader: (
    SchoolViewState,
    SchoolViewEvents.LoadSchoolData,
) -> SchoolViewState = { state, event -> state.copy(id = "school 1", name = "School One") }

class SchoolStateMachine internal constructor(
    dataLoader: StateUpdate<SchoolViewState, SchoolViewEvents.LoadSchoolData>,
    studentLoader: AsyncDataUpdate<LoadStudentsEvent, SomeRandomError, List<Person>>,
    teacherLoader: AsyncDataUpdate<LoadTeachersEvent, SomeRandomError, List<Person>>,
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
            loadStudent: AsyncDataUpdate<LoadStudentsEvent, SomeRandomError, List<Person>> = studentLoader,
            loadTeacher: AsyncDataUpdate<LoadTeachersEvent, SomeRandomError, List<Person>> = teacherLoader,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        ) = SchoolStateMachine(loadSchoolData, loadStudent, loadTeacher, scope)
    }
}
