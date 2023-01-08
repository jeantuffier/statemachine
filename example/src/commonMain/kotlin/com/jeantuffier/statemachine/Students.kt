package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewActionsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@ViewState
data class StudentsViewState(
    val studentCount: Int = 0,

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

@ViewActionsBuilder(
    className = "StudentsViewActions",
    crossActions = [LoadStudentsAction::class]
)
sealed class StudentsViewActionsBuilder {
    object LoadStudentCount : StudentsViewActionsBuilder()
}

private val studentCountLoader: (
    MutableStateFlow<StudentsViewState>,
    StudentsViewEvents.LoadStudentCount
) -> Unit = { state, _ ->
    state.update {
        it.copy(studentCount = 1000)
    }
}

/*
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
*/
