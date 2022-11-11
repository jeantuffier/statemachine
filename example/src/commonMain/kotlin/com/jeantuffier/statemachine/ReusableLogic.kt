package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossViewEvent
import com.jeantuffier.statemachine.framework.loadAsyncData
import kotlin.native.concurrent.SharedImmutable

@CrossViewEvent
interface LoadStudentsEvent {
    val offset: Int
    val limit: Int
}

@CrossViewEvent
interface LoadTeachersEvent {
    val offset: Int
    val limit: Int
}

@SharedImmutable
val loadStudents = loadAsyncData(TransitionKey.students) { _: LoadStudentsEvent ->
    Either.Right(listOf(Person("student1", "student1"), Person("student2", "student2")))
}

@SharedImmutable
val loadTeachers = loadAsyncData(TransitionKey.teachers) { _: LoadTeachersEvent ->
    Either.Right(listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2")))
}
