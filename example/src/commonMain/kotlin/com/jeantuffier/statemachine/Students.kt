package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.StateMachine
import com.jeantuffier.statemachine.framework.StateMachineBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ViewState
data class StudentsViewState(
    val studentCount: Int = 0,

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

object LoadStudentCount

@ViewEventsBuilder(
    crossViewEvents = [
        LoadStudentsEvent::class,
        LoadStudentCount::class,
    ]
)
class StudentsViewEventsBuilder

class StudentsStateMachine(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : StateMachine<StudentsViewState, StudentsViewEvents> by StateMachineBuilder(
    initialValue = StudentsViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = StudentsViewStateUpdater(state)
        when (event) {
            is StudentsViewEvents.LoadStudentCount -> {
                state.update { it.copy(studentCount = 2000) }
            }

            is StudentsViewEvents.LoadStudentsEvent -> launch { updater.loadStudents(event, studentLoader) }
        }
    }
)
