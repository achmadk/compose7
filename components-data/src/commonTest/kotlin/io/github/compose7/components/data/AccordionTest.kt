package io.github.compose7.components.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Expand/collapse bookkeeping behind [Accordion] (PRD §7 "Data display" P1):
 * single mode replaces the open row, multiple mode accumulates, and toggling an
 * open row always closes it.
 */
class AccordionTest {

    @Test
    fun singleModeReplacesTheOpenRow() {
        assertEquals(setOf(2), toggledIndices(setOf(1), index = 2, mode = ExpandMode.Single))
    }

    @Test
    fun singleModeClosesAnOpenRow() {
        assertEquals(emptySet(), toggledIndices(setOf(2), index = 2, mode = ExpandMode.Single))
    }

    @Test
    fun multipleModeAccumulatesRows() {
        assertEquals(setOf(0, 2), toggledIndices(setOf(0), index = 2, mode = ExpandMode.Multiple))
    }

    @Test
    fun multipleModeClosesOnlyTheTappedRow() {
        assertEquals(setOf(0, 3), toggledIndices(setOf(0, 2, 3), index = 2, mode = ExpandMode.Multiple))
    }

    @Test
    fun openingFromEmptyWorksInBothModes() {
        assertEquals(setOf(1), toggledIndices(emptySet(), index = 1, mode = ExpandMode.Single))
        assertEquals(setOf(1), toggledIndices(emptySet(), index = 1, mode = ExpandMode.Multiple))
    }
}
