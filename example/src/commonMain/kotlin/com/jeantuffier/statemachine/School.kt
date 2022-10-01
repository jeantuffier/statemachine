package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewEventsBuilder
import com.jeantuffier.statemachine.annotation.ViewState
import com.jeantuffier.statemachine.framework.AsyncData
import com.jeantuffier.statemachine.framework.StateMachine
import com.jeantuffier.statemachine.framework.StateMachineBuilder

@ViewState
data class SchoolViewState(
    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    @CrossStateProperty(key = "students")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

@ViewEventsBuilder(
    crossViewEvents = [
        LoadStudentsInterface::class,
        LoadTeachersInterface::class,
    ]
)
class SchoolViewEventsBuilder

class SchoolStateMachine : StateMachine<SchoolViewState, SchoolViewEvents> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    reducer = { state, event ->
        val updater = SchoolViewStateUpdater(state)
        when (event) {
            is SchoolViewEvents.LoadStudents -> loadStudents(updater, event)
            is SchoolViewEvents.LoadTeachers -> loadTeachers(updater, event)
        }
    }
)
