package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossViewEvent
import com.jeantuffier.statemachine.framework.loadAsyncData
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
val loadStudents = loadAsyncData(
    key = TransitionKey.students,
    loader = ::loadStudentFromRemoteServer,
)

private fun loadStudentFromRemoteServer(event: LoadStudentsInterface): Either<AppError, List<Person>> =
    Either.Right(listOf(Person("student1", "student1"), Person("student2", "student2")))

@SharedImmutable
val loadTeachers = loadAsyncData(
    key = TransitionKey.teachers,
    loader = ::loadTeachersFromRemoteServer,
)

private fun loadTeachersFromRemoteServer(event: LoadTeachersInterface): Either<AppError, List<Person>> =
    Either.Right(listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2")))
