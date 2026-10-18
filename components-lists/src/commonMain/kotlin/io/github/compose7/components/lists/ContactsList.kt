package io.github.compose7.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography

/** One contact in a [ContactsList] (PRD §7, P1 / Appendix A "Contacts List"). */
data class ContactRow(
    /** Full display name. */
    val name: String,
    /** 1–2 character avatar fallback, drawn as text (no icon assets, PRD §4). */
    val initials: String,
    /** Optional detail line — phone number, email, … */
    val subtitle: String? = null,
)

/** A titled group of contacts, typically one alphabet letter. */
data class ContactSection(
    val title: String,
    val items: List<ContactRow>,
)

/** Avatar diameter + trailing gap reserved for the leading slot. */
private val contactsLeadingSize = 44.dp + 12.dp

/**
 * Contacts-style grouped list preset (PRD §7, P1 / Appendix A "Contacts
 * List"): sections titled by letter, round initials avatars, disclosure
 * chevrons — a [List] of [ListSection]s of [ListItem]s under the hood.
 *
 * ```kotlin
 * ContactsList(
 *     sections = listOf(
 *         ContactSection("A", listOf(ContactRow("Alice", "AL", "+1 555 0100"))),
 *         ContactSection("B", listOf(ContactRow("Bob", "BO"))),
 *     ),
 *     onContactClick = { contact -> open(contact) },
 * )
 * ```
 *
 * @param sections Letter groups to render, in order.
 * @param style Plain or grouped; `null` follows the active style's default
 *   (grouped on Cupertino — the idiomatic contacts look).
 * @param avatar Custom avatar slot per row; `null` renders the default
 *   initials-in-a-circle avatar.
 * @param onContactClick Row click; `null` renders non-interactive rows.
 */
@Composable
fun ContactsList(
    modifier: Modifier = Modifier,
    sections: List<ContactSection> = emptyList(),
    style: ListStyle? = null,
    avatar: (@Composable (ContactRow) -> Unit)? = null,
    onContactClick: ((ContactRow) -> Unit)? = null,
) {
    List(modifier = modifier, style = style) {
        sections.forEach { section ->
            ListSection(title = section.title) {
                section.items.forEach { contact ->
                    ListItem(
                        title = contact.name,
                        subtitle = contact.subtitle,
                        leading = {
                            if (avatar != null) {
                                avatar(contact)
                            } else {
                                InitialsAvatar(contact.initials)
                            }
                        },
                        leadingSize = contactsLeadingSize,
                        showChevron = true,
                        onClick = onContactClick?.let { click -> ({ click(contact) }) },
                    )
                }
            }
        }
    }
}

/** Default avatar: initials as text in a tinted circle — never an icon asset (PRD §4). */
@Composable
private fun InitialsAvatar(
    initials: String,
    size: Dp = 44.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Compose7ThemeColors.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = initials,
            style = Compose7ThemeTypography.labelLarge.copy(
                color = Compose7ThemeColors.onSurfaceVariant,
            ),
        )
    }
}
