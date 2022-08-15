package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossStateProperty
import com.jeantuffier.statemachine.annotation.ViewState

@ViewState
data class SchoolStaffViewState(
    val staffCount: Int,

    @CrossStateProperty(key = "teachers")
    val teachers: AsyncData<List<Person>> = AsyncData(emptyList()),

    val adminEmployees: AsyncData<List<Person>> = AsyncData(emptyList()),
)
