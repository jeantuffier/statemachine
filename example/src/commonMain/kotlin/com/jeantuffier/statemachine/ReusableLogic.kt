package com.jeantuffier.statemachine

import arrow.core.Either
import com.jeantuffier.statemachine.annotation.CrossAction
import com.jeantuffier.statemachine.framework.AsyncDataUpdate
import kotlin.native.concurrent.SharedImmutable

@CrossAction
interface LoadStudentsAction {
    val offset: Int
    val limit: Int
}

@CrossAction
interface LoadTeachersAction {
    val offset: Int
    val limit: Int
}

@SharedImmutable
val studentLoader: AsyncDataUpdate<LoadStudentsAction, SomeRandomError, List<Person>> = {
    Either.Right(listOf(Person("student1", "student1"), Person("student2", "student2")))
}

@SharedImmutable
val teacherLoader: suspend (LoadTeachersAction) -> Either<SomeRandomError, List<Person>> = {
    Either.Right(listOf(Person("teacher1", "teacher1"), Person("teacher2", "teacher2")))
}
