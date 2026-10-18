package io.github.compose7.components.lists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Divider

/**
 * Layout facts resolved by the enclosing [List] / [VirtualList] that rows,
 * sections, and buttons read so a single row API renders correctly in both
 * plain and grouped contexts (PRD §8.3: platform-correct insets).
 */
internal data class ListLayoutInfo(
    val style: ListStyle,
    val contentInset: Dp,
)

/** Provided by [List]/[VirtualList]; `null` means "no list ancestor — use style defaults". */
internal val LocalListLayout = compositionLocalOf<ListLayoutInfo?> { null }

/** The effective list style: the enclosing list's, else the per-style default. */
@Composable
internal fun currentListStyle(): ListStyle =
    LocalListLayout.current?.style ?: defaultListStyle()

/** Cupertino defaults to grouped; Material and DesktopUI7 default to plain (PRD §8.3). */
@Composable
internal fun defaultListStyle(): ListStyle =
    if (currentCompose7Style == Compose7Style.Cupertino) ListStyle.Grouped else ListStyle.Plain

/** Effective content inset: the enclosing list's, else the style's `listContentInset`. */
@Composable
internal fun currentListContentInset(): Dp =
    LocalListLayout.current?.contentInset ?: Compose7StyleInfo.listContentInset

/**
 * Row separator with platform-correct inset (PRD §8.3): edge-to-edge on
 * Material/DesktopUI7, content-inset on Cupertino. [leadingInset] adds the
 * width of the row's leading-media slot (icon/avatar) so dividers line up
 * with the title on grouped iOS rows.
 */
@Composable
internal fun ListRowDivider(leadingInset: Dp = 0.dp) {
    Divider(insetStart = Compose7StyleInfo.listDividerInset + leadingInset)
}
