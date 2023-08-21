package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ScrollBarSpec(
    val color: Color = Color(0xB2888888),
    val size: Dp = 3.dp,
    val margin: Dp = 6.dp
) {
    companion object {
        val Default = ScrollBarSpec()
    }
}