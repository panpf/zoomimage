package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Used to configure the style of the scroll bar
 */
@Immutable
data class ScrollBarSpec(
    /**
     * Scroll bar color, which defaults to translucent gray
     */
    val color: Color = Color(0xB2888888),

    /**
     * Scroll bar size, default to 3 dp
     */
    val size: Dp = 3.dp,

    /**
     * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
     */
    val margin: Dp = 6.dp
) {
    companion object {
        val Default = ScrollBarSpec()
    }
}