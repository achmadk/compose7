package io.github.compose7.components.forms

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Filtering behind [Autocomplete] (PRD §13: autocomplete filter logic gets
 * unit tests rather than living only inside the composable).
 */
class AutocompleteFilterTest {

    private val cities = listOf("Amsterdam", "Athens", "Berlin", "Lisbon", "Oslo", "Prague", "Rome")

    @Test
    fun queryBelowMinimumLengthReturnsNothing() {
        assertEquals(emptyList(), filterSuggestions("a", cities, minQueryLength = 2))
        assertEquals(emptyList(), filterSuggestions("", cities))
    }

    @Test
    fun matchingIsCaseInsensitiveAndSubstringBased() {
        assertEquals(listOf("Amsterdam"), filterSuggestions("am", cities))
        assertEquals(listOf("Berlin", "Lisbon"), filterSuggestions("LI", cities))
        assertEquals(listOf("Rome"), filterSuggestions("ome", cities))
    }

    @Test
    fun sourceOrderIsPreservedRatherThanRanked() {
        // Only these three contain "o"; output must follow the input list.
        assertEquals(
            listOf("Lisbon", "Oslo", "Rome"),
            filterSuggestions("o", cities),
        )
    }

    @Test
    fun limitCapsWithoutReordering() {
        assertEquals(listOf("Amsterdam", "Athens", "Berlin"), filterSuggestions("", cities, minQueryLength = 0, limit = 3))
        assertEquals(emptyList(), filterSuggestions("a", cities, minQueryLength = 0, limit = 0))
        assertEquals(emptyList(), filterSuggestions("a", cities, minQueryLength = 0, limit = -1))
    }

    @Test
    fun unknownQueryReturnsNothing() {
        assertEquals(emptyList(), filterSuggestions("zzz", cities))
    }
}
