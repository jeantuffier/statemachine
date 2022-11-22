package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.StateMachine
import com.jeantuffier.statemachine.framework.StateMachineBuilder
import com.jeantuffier.statemachine.framework.defaultStateMachineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

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
    private val scope: CoroutineScope = defaultStateMachineScope()
) : StateMachine<StudentsViewState, StudentsViewEvents> by StateMachineBuilder(
    initialValue = StudentsViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = StudentsViewStateUpdater(state)
        when (event) {
            is StudentsViewEvents.LoadStudentCount -> {
                state.update { it.copy(studentCount = 2000) }
            }

            is StudentsViewEvents.LoadStudentsEvent -> loadStudents(updater, event)
        }
    }
)
