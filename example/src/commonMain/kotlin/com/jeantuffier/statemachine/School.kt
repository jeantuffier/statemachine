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

class SchoolStateMachine(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : StateMachine<SchoolViewState, SchoolViewEvents> by StateMachineBuilder(
    initialValue = SchoolViewState(),
    scope = scope,
    reducer = { state, event ->
        val updater = SchoolViewStateUpdater(state)
        when (event) {
            is SchoolViewEvents.LoadSchoolData -> {
                state.update {
                    it.copy(id = "school 1", name = "School One")
                }
            }

            is SchoolViewEvents.LoadStudentsEvent -> loadStudents(updater, event)
            is SchoolViewEvents.LoadTeachersEvent -> loadTeachers(updater, event)
        }
    }
)
