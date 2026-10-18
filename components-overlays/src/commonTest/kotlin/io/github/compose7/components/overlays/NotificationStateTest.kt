package io.github.compose7.components.overlays

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Queue behind [NotificationHost] (PRD §8.4: notification stacking). */
class NotificationStateTest {

    @Test
    fun showQueuesBannersOldestFirst() {
        val state = NotificationState()
        val first = state.show("One")
        state.show("Two", text = "Body", durationMillis = 2_000)

        assertEquals(listOf("One", "Two"), state.messages.map { it.title })
        assertEquals("Body", state.messages[1].text)
        assertEquals(2_000L, state.messages[1].durationMillis)
        assertTrue(state.messages[1].id > first)
    }

    @Test
    fun bannersArePersistentUnlessADurationIsGiven() {
        val state = NotificationState()
        state.show("Persistent")
        assertEquals(null, state.messages.single().durationMillis)
    }

    @Test
    fun dismissRemovesOnlyThatBanner() {
        val state = NotificationState()
        val first = state.show("One")
        state.show("Two")

        state.dismiss(first)

        assertEquals(listOf("Two"), state.messages.map { it.title })
    }

    @Test
    fun clearEmptiesTheStack() {
        val state = NotificationState()
        state.show("One")
        state.show("Two")

        state.clear()

        assertNull(state.messages.firstOrNull())
    }
}
