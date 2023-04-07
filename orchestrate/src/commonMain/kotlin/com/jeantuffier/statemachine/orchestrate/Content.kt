package com.jeantuffier.statemachine.orchestrate

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

data class Content<T>(
    val isLoading: Boolean = false,
    val value: T? = null
)

data class PagingContent<T>(
    val available: Available = Available(0),
    val isLoading: Boolean = false,
    val items: List<T> = emptyList()
)

fun <T> PagingContent<T>.hasLoadedEverything() = items.isNotEmpty() && available.value == items.size

@Serializable
data class Page<T>(
    val offset: Offset = Offset(0),
    val limit: Limit = Limit(0),
    val available: Available = Available(0),
    val items: List<T> = emptyList()
)

@JvmInline
@Serializable
value class Offset(val value: Int)

@JvmInline
@Serializable
value class Limit(val value: Int)

@JvmInline
@Serializable
value class Available(val value: Int)