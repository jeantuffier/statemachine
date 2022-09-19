package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import kotlinx.coroutines.flow.update
import kotlin.native.concurrent.SharedImmutable

@ViewState
data class StudentsViewState(
    val studentCount: Int = 0,

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

object LoadStudentCount

@ViewEventsBuilder(
    crossViewEvents = [
        LoadStudents::class,
        LoadStudentCount::class,
    ]
)
class StudentsViewEventsBuilder

class StudentsStateMachine : StateMachine<StudentsViewState, StudentsViewEvents> by StateMachineBuilder(
    initialValue = StudentsViewState(),
    reducer = { state, event ->
        val updater = StudentsViewStateUpdater(state)
        when (event) {
            is StudentsViewEvents.LoadStudentCount -> loadStudentCount(state, event)
            is StudentsViewEvents.LoadStudents -> loadStudents(updater, event as LoadStudents)
        }
    }
)

@SharedImmutable
val loadStudentCount = ViewStateTransition<StudentsViewState, StudentsViewEvents.LoadStudentCount> { state, event ->
    state.update { it.copy(studentCount = 2000) }
}
