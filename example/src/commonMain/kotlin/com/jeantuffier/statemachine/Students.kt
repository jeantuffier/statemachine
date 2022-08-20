package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewState
import kotlinx.coroutines.flow.update
import kotlin.native.concurrent.SharedImmutable

@ViewState
data class StudentsViewState(
    val studentCount: Int = 0,

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

class StudentsStateMachine : StateMachine<StudentsViewState, AppEvent> by StateMachineBuilder(
    initialValue = StudentsViewState(),
    reducer = { state, event ->
        val updater = StudentsViewStateUpdater(state)
        when (event) {
            is AppEvent.StudentsEvent.LoadCount -> loadStudentCount(state, event)
            is AppEvent.LoadTeachers -> loadTeachers(updater, event)
            else -> {}
        }
    }
)

@SharedImmutable
val loadStudentCount = ViewStateTransition<StudentsViewState, AppEvent.StudentsEvent.LoadCount> { state, event ->
    state.update { it.copy(studentCount = 2000) }
}
