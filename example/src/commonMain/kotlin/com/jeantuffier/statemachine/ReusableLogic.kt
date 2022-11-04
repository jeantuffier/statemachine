package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossViewEvent
import kotlin.native.concurrent.SharedImmutable

@CrossViewEvent
interface LoadStudentsInterface {
    val offset: Int
    val limit: Int
}

@CrossViewEvent
interface LoadTeachersInterface {
    val offset: Int
    val limit: Int
}

@SharedImmutable
val loadStudents = loadAsyncData<LoadStudentsInterface, AppError, List<Person>>(
    key = TransitionKey.students,
    loader = { event -> Either.Right(listOf(Person("student1", "student1"), Person("student2", "student2"))) },
)

@SharedImmutable
val loadTeachers = loadAsyncData<LoadTeachersInterface, AppError, List<Person>>(
    key = TransitionKey.teachers,
    loader = { event -> Either.Right(listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2"))) },
)
