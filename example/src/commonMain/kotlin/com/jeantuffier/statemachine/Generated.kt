package com.jeantuffier.statemachine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


//enum class TransitionKey { STUDENTS, TEACHERS }

//class SchoolViewStateUpdater(
//    private val mutableStateFlow: MutableStateFlow<SchoolViewState>,
//) : ViewStateUpdater<TransitionKey> {
//    override fun <T> currentValue(key: TransitionKey) =
//        when (key) {
//            TransitionKey.STUDENTS -> mutableStateFlow.value.students as T
//            TransitionKey.TEACHERS -> mutableStateFlow.value.teachers as T
//        }
//
//    override fun updateValue(key: TransitionKey, newValue: Any) =
//        when (key) {
//            TransitionKey.STUDENTS -> mutableStateFlow.update { it.copy(students = newValue as AsyncData<List<Person>>) }
//            TransitionKey.TEACHERS -> mutableStateFlow.update { it.copy(teachers = newValue as AsyncData<List<Person>>) }
//        }
//
//    override fun updateValues(values: Map<TransitionKey, Any>) =
//        values.entries.forEach { updateValue(it.key, it.value) }
//}

//class SchoolStaffViewStateUpdater(
//    private val mutableStateFlow: MutableStateFlow<SchoolStaffViewState>,
//) : ViewStateUpdater<TransitionKey> {
//    override fun <T> currentValue(key: TransitionKey) =
//        when (key) {
//            TransitionKey.STUDENTS -> throw Exception("This key cannot be used on this view state")
//            TransitionKey.TEACHERS -> mutableStateFlow.value.teachers as T
//        }
//
//    override fun updateValue(key: TransitionKey, newValue: Any) =
//        when (key) {
//            TransitionKey.STUDENTS -> {}
//            TransitionKey.TEACHERS -> mutableStateFlow.update { it.copy(teachers = newValue as AsyncData<List<Person>>) }
//        }
//
//    override fun updateValues(values: Map<TransitionKey, Any>) =
//        values.entries.forEach { updateValue(it.key, it.value) }
//}