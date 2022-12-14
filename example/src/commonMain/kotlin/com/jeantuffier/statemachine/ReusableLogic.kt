package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossViewEvent
import com.jeantuffier.statemachine.framework.*
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
val studentLoader: suspend (LoadStudentsEvent) -> Either<SomeRandomError, List<Person>> = {
    Either.Right(listOf(Person("student1", "student1"), Person("student2", "student2")))
}

@SharedImmutable
val teacherLoader: suspend (LoadTeachersEvent) -> Either<SomeRandomError, List<Person>> = {
    Either.Right(listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2")))
}
