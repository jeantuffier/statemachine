package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewState

@ViewState
data class SchoolViewState(
    @CrossStateProperty(key = "TEACHERS")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    @CrossStateProperty(key = "STUDENTS")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

class SchoolStateMachine : StateMachine<SchoolViewState, AppEvent> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    reducer = { state, event ->
        val updater = SchoolViewStateUpdater(state)
        when (event) {
            is AppEvent.LoadStudents -> loadStudents(updater, event)
            is AppEvent.LoadTeachers -> loadTeachers(updater, event)
        }
    }
)
