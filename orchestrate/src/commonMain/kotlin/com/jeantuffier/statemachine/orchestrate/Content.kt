package com.jeantuffier.statemachine.orchestrate

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Contains the data returned by a [com.jeantuffier.statemachine.orchestrate.OrchestratedUpdate] or
 * [com.jeantuffier.statemachine.orchestrate.OrchestratedFlowUpdate] when the property annotated with [Orchestrated]
 * and a type [OrchestratedPage].
 *
 * Pagination is supported and implemented out of the box regardless of the expected length of the result.
 *
 * @param available The number of item available in the source.
 * @param offset The offset position where the sublist of items from this page starts compared to the complete list.
 * @param limit The maximum amount of items that can be contained in the sublist of this page.
 * @param items The sublist of items representing the current page.
 */
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

/**
 * A property annotated with [Orchestrated] representing a single item should use [OrchestratedData] as its type.
 *
 * @param isLoading Is true when the generated [com.jeantuffier.statemachine.core.Reducer] reduces the input associated
 * with this property, false the rest of the time.
 * @param value Is the value of this property.
 */
@Serializable
data class OrchestratedData<T>(
    val isLoading: Boolean = false,
    val value: T? = null,
)

/**
 * A property annotated with [Orchestrated] and representing a collection of items should use [OrchestratedData] as its
 * type.
 *
 * @param isLoading Is true when the generated [com.jeantuffier.statemachine.core.Reducer] reduces the input associated
 * with this property, false the rest of the time.
 * @param pages Is a map containing the data loaded for this property. Each entry represent a page where
 * - the key is the page index
 * - the value is the list of items for this page.
 */
@Serializable
data class OrchestratedPage<T>(
    val available: Available = Available(0),
    val isLoading: Boolean = false,
    val pages: Map<Int, List<T>> = emptyMap(),
) {

    /**
     * A shortcut to obtain all the items in the map at once.
     */
    fun items(): List<T> = pages.values.flatten()

    /**
     * Return true if the amount of all the items in the map equals the available value, false otherwise.
     */
    fun hasLoadedEverything(): Boolean {
        val items = items()
        return items.isNotEmpty() && available.value == items.size
    }
}
