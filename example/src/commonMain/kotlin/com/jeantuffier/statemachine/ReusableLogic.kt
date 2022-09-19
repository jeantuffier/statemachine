package com.jeantuffier.statemachine

import com.jeantuffier.statemachine.annotation.CrossViewEvent
import com.jeantuffier.statemachine.loadAsyncData
import kotlin.native.concurrent.SharedImmutable

@CrossViewEvent
data class LoadStudents(val offset: Int, val limit: Int)

@CrossViewEvent
data class LoadTeachers(val offset: Int, val limit: Int)

@SharedImmutable
val loadStudents = loadAsyncData<List<Person>, LoadStudents>(
    key = TransitionKey.students,
    loader = { event -> listOf(Person("student1", "student1"), Person("student2", "student2")) },
)

@SharedImmutable
val loadTeachers = loadAsyncData<List<Person>, LoadTeachers>(
    key = TransitionKey.teachers,
    loader = { event -> listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2")) },
)
