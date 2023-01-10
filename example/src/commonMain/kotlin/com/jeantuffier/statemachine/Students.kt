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

    @CrossStateProperty
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
    StudentsViewActions.LoadStudentCount
) -> Unit = { state, _ ->
    state.update {
        it.copy(studentCount = 1000)
    }
}
