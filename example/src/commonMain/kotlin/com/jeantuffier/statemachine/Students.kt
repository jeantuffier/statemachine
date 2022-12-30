package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.StateMachine
import com.jeantuffier.statemachine.framework.StateMachineBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ViewState
data class StudentsViewState(
    val studentCount: Int = 0,

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

@ViewEventsBuilder(
    crossViewEvents = [LoadStudentsEvent::class]
)
sealed class StudentsViewEventsBuilder {
    object LoadStudentCount : StudentsViewEventsBuilder()
}

private val studentCountLoader: (
    MutableStateFlow<StudentsViewState>,
    StudentsViewEvents.LoadStudentCount
) -> Unit = { state, _ ->
    state.update {
        it.copy(studentCount = 1000)
    }
}

class StudentsStateMachine(
    loadStudentCount: (MutableStateFlow<StudentsViewState>, StudentsViewEvents.LoadStudentCount) -> Unit = studentCountLoader,
    loadStudent: suspend (LoadStudentsEvent) -> Either<SomeRandomError, List<Person>> = studentLoader,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : StateMachine<StudentsViewState, StudentsViewEvents> by StateMachineBuilder(
    initialValue = StudentsViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = StudentsViewStateUpdater(state)
        when (event) {
            is StudentsViewEvents.LoadStudentCount -> loadStudentCount(state, event)
            is StudentsViewEvents.LoadStudentsEvent -> scope.launch { updater.loadStudents(event, loadStudent) }
        }
    }
)
