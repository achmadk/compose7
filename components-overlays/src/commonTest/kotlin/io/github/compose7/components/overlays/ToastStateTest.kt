package io.github.compose7.components.overlays

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Queue and stacking order behind [ToastHost] (PRD §8.4: toast stacking).
 */
class ToastStateTest {

    @Test
    fun showQueuesInOrderAndReturnsIncreasingIds() {
        val state = ToastState()
        val first = state.show("one")
        val second = state.show("two")

        assertTrue(second > first)
        assertEquals(listOf("one", "two"), state.messages.map { it.text })
    }

    @Test
    fun dismissRemovesOnlyThatMessage() {
        val state = ToastState()
        val first = state.show("one")
        state.show("two")

        state.dismiss(first)

        assertEquals(listOf("two"), state.messages.map { it.text })
    }

    @Test
    fun dismissUnknownIdIsANoOp() {
        val state = ToastState()
        state.show("one")

        state.dismiss(999)

        assertEquals(1, state.messages.size)
    }

    @Test
    fun clearEmptiesTheQueue() {
        val state = ToastState()
        state.show("one")
        state.show("two")

        state.clear()

        assertTrue(state.messages.isEmpty())
    }

    @Test
    fun bottomStackKeepsSourceOrderSoTheNewestSitsAtTheEdge() {
        val messages = listOf(
            ToastMessage(1, "old"),
            ToastMessage(2, "mid"),
            ToastMessage(3, "new"),
        )

        assertEquals(
            listOf("old", "mid", "new"),
            visibleMessages(messages, ToastPosition.Bottom, maxVisible = 3).map { it.text },
        )
    }

    @Test
    fun topStackReversesSoTheNewestSitsAtTheEdge() {
        val messages = listOf(
            ToastMessage(1, "old"),
            ToastMessage(2, "mid"),
            ToastMessage(3, "new"),
        )

        assertEquals(
            listOf("new", "mid", "old"),
            visibleMessages(messages, ToastPosition.Top, maxVisible = 3).map { it.text },
        )
    }

    @Test
    fun onlyTheNewestWindowIsShown() {
        val messages = (1..5).map { ToastMessage(it.toLong(), "t$it") }

        assertEquals(
            listOf("t3", "t4", "t5"),
            visibleMessages(messages, ToastPosition.Bottom, maxVisible = 3).map { it.text },
        )
        assertEquals(
            listOf("t5", "t4", "t3"),
            visibleMessages(messages, ToastPosition.Top, maxVisible = 3).map { it.text },
        )
    }

    @Test
    fun nonPositiveLimitShowsNothing() {
        val messages = listOf(ToastMessage(1, "one"))
        assertTrue(visibleMessages(messages, ToastPosition.Bottom, maxVisible = 0).isEmpty())
    }
}
