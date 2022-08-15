package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewState
import kotlinx.coroutines.flow.update
import kotlin.native.concurrent.SharedImmutable

@ViewState
data class StudentsViewState(
    val studentCount: Int,

    @CrossStateProperty(key = "STUDENTS")
    val students: AsyncData<List<Person>> = AsyncData(emptyList()),
)

@SharedImmutable
val loadStudentCount = ViewStateTransition<StudentsViewState, AppEvent.StudentsEvent.LoadCount> { state, event ->
    state.update { it.copy(studentCount = 2000) }
}
