package com.github.panpf.zoomimage.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ScrollBar(
    val color: Color = Color.Gray.copy(alpha = 0.7f),
    val size: Dp = 3.dp,
    val margin: Dp = 6.dp
) {
    companion object {
        val Default = ScrollBar()
    }
}