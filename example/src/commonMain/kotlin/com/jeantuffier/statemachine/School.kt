package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState

@ViewState
data class SchoolViewState(
    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

@ViewEventsBuilder(
    crossViewEvents = [
        LoadStudents::class,
        LoadTeachers::class,
    ]
)
class SchoolViewEventsBuilder

class SchoolStateMachine : StateMachine<SchoolViewState, SchoolViewEvents> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    reducer = { state, event ->
        val updater = SchoolViewStateUpdater(state)
        when (event) {
            is SchoolViewEvents.LoadStudents -> loadStudents(updater, event as LoadStudents)
            is SchoolViewEvents.LoadTeachers -> loadTeachers(updater, event as LoadTeachers)
        }
    }
)
