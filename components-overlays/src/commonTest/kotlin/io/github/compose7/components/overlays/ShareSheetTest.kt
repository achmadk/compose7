package io.github.compose7.components.overlays

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pure-fallback rows behind [ShareSheet] (PRD §13). The important contract is
 * ordering: notify the caller for the selected item, then dismiss; Cancel
 * dismisses without notifying.
 */
class ShareSheetTest {

    private val items = listOf(
        ShareItem("Copy text", "Compose7"),
        ShareItem("Copy link", "https://example.com", ShareItemKind.Link),
    )

    @Test
    fun rowsFollowItemOrderAndEndWithOneCancel() {
        val actions = shareSheetFallbackActions(items, {}, {})
        assertEquals(listOf("Copy text", "Copy link", "Cancel"), actions.map { it.text })
        assertEquals(1, actions.count { it.role == ActionSheetActionRole.Cancel })
        assertEquals(ActionSheetActionRole.Cancel, actions.last().role)
    }

    @Test
    fun selectingAnItemNotifiesThenDismisses() {
        val events = mutableListOf<String>()
        val actions = shareSheetFallbackActions(
            items = items,
            onItemSelected = { events += "selected:${it.label}" },
            onDismissRequest = { events += "dismissed" },
        )

        actions.first { it.text == "Copy link" }.onClick()

        assertEquals(listOf("selected:Copy link", "dismissed"), events)
    }

    @Test
    fun cancelDismissesWithoutSelecting() {
        val events = mutableListOf<String>()
        val actions = shareSheetFallbackActions(
            items = items,
            onItemSelected = { events += "selected:${it.label}" },
            onDismissRequest = { events += "dismissed" },
        )

        actions.last().onClick()

        assertEquals(listOf("dismissed"), events)
    }

    @Test
    fun cancelLabelIsCallerOwned() {
        val actions = shareSheetFallbackActions(items, {}, {}, cancelLabel = "Batal")
        assertEquals("Batal", actions.last().text)
        assertEquals(ActionSheetActionRole.Cancel, actions.last().role)
    }

    @Test
    fun emptyItemsStillOfferCancel() {
        val actions = shareSheetFallbackActions(emptyList(), {}, {})
        assertEquals(listOf("Cancel"), actions.map { it.text })
    }
}
