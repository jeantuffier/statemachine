package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.StateMachine
import com.jeantuffier.statemachine.framework.StateMachineBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class SchoolStateMachine(
    loadSchoolData: (SchoolViewState, SchoolViewEvents.LoadSchoolData) -> SchoolViewState = schoolDataLoader,
    loadStudent: suspend (LoadStudentsEvent) -> Either<SomeRandomError, List<Person>> = studentLoader,
    loadTeacher: suspend (LoadTeachersEvent) -> Either<SomeRandomError, List<Person>> = teacherLoader,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : StateMachine<SchoolViewState, SchoolViewEvents> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = SchoolViewStateUpdater(state)
        when (event) {
            is SchoolViewEvents.LoadSchoolData -> state.update { loadSchoolData(state.value, event) }
            is SchoolViewEvents.LoadStudentsEvent -> scope.launch {
                updater.loadStudents(
                    event,
                    loadStudent
                )
            }

            is SchoolViewEvents.LoadTeachersEvent -> scope.launch {
                updater.loadTeachers(
                    event,
                    loadTeacher
                )
            }
        }
    }
)


class SchoolStateMachineTestImp(
    private val loadSchoolData: (SchoolViewState, SchoolViewEvents.LoadSchoolData) -> SchoolViewState = schoolDataLoader,
    private val loadStudent: suspend (LoadStudentsEvent) -> Either<SomeRandomError, List<Person>> = studentLoader,
    private val loadTeacher: suspend (LoadTeachersEvent) -> Either<SomeRandomError, List<Person>> = teacherLoader,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : StateMachine<SchoolViewState, SchoolViewEvents> {
    private val _state: MutableStateFlow<SchoolViewState> = MutableStateFlow(SchoolViewState())
    override val state: StateFlow<SchoolViewState> = _state.asStateFlow()

    override fun <T : SchoolViewEvents> reduce(event: T) {
        val updater = SchoolViewStateUpdater(_state)
        when (event) {
            is SchoolViewEvents.LoadSchoolData -> _state.update { loadSchoolData(state.value, event) }
            is SchoolViewEvents.LoadStudentsEvent -> scope.launch {
                updater.loadStudents(
                    event,
                    loadStudent
                )
            }

            is SchoolViewEvents.LoadTeachersEvent -> scope.launch {
                updater.loadTeachers(
                    event,
                    loadTeacher
                )
            }
        }
    }

    override fun close() {
        scope.cancel()
    }
}