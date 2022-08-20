package com.jeantuffier.statemachine

import kotlin.native.concurrent.SharedImmutable

sealed class AppEvent {
    data class LoadStudents(val offset: Int, val limit: Int) : AppEvent()
    data class LoadTeachers(val offset: Int, val limit: Int) : AppEvent()

    sealed class StudentsEvent: AppEvent() {
        object LoadCount : StudentsEvent()
    }

    sealed class SchoolStaffEvent: AppEvent() {
        object LoadStaffCount : SchoolStaffEvent()
        data class LoadAdminEmployees(val offset: Int, val limit: Int) : SchoolStaffEvent()
    }
}

@SharedImmutable
val loadStudents = loadAsyncData<List<Person>, AppEvent.LoadStudents>(
    key = TransitionKey.students,
    loader = { event -> listOf(Person("student1", "student1"), Person("student2", "student2")) },
)

@SharedImmutable
val loadTeachers = loadAsyncData<List<Person>, AppEvent.LoadTeachers>(
    key = TransitionKey.teachers,
    loader = { event -> listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2")) },
)
