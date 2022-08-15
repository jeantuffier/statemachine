package com.jeantuffier.statemachine

import kotlin.native.concurrent.SharedImmutable

sealed class AppEvent {
    data class LoadStudents(val offset: Int, val limit: Int) : AppEvent()
    data class LoadTeachers(val offset: Int, val limit: Int) : AppEvent()

    sealed class StudentsEvent {
        object LoadCount : StudentsEvent()
    }

    sealed class SchoolStaffEvent {
        object LoadStaffCount : SchoolStaffEvent()
        data class LoadAdminEmployees(val offset: Int, val limit: Int) : SchoolStaffEvent()
    }
}

//@SharedImmutable
//val loadStudents = loadAsyncData<TransitionKey, List<Person>, AppEvent.LoadStudents>(
//    key = TransitionKey.STUDENTS,
//    loader = { event -> listOf(Person("student1", "student1"), Person("student2", "student2")) },
//)
//
//@SharedImmutable
//val loadTeachers = loadAsyncData<TransitionKey, List<Person>, AppEvent.LoadTeachers>(
//    key = TransitionKey.STUDENTS,
//    loader = { event -> listOf(Person("student1", "student1"), Person("student2", "student2")) },
//)
