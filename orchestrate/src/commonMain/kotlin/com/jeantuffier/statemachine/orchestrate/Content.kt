package com.jeantuffier.statemachine.orchestrate

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Page<T>(
    val available: Available = Available(0),
    val offset: Offset = Offset(0),
    val limit: Limit = Limit(20),
    val items: List<T> = emptyList(),
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

@Serializable
data class OrchestratedData<T>(
    val isLoading: Boolean = false,
    val value: T? = null,
)

@Serializable
data class OrchestratedPage<T>(
    val available: Available = Available(0),
    val isLoading: Boolean = false,
    val pages: Map<Int, List<T>> = emptyMap(),
) {
    fun items(): List<T> = pages.values.flatten()

    fun hasLoadedEverything(): Boolean {
        val items = items()
        return items.isNotEmpty() && available.value == items.size
    }
}
