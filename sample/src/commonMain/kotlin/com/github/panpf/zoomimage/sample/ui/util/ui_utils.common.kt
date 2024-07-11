package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

@Composable
expect fun windowSize(): IntSize

internal fun Size.toIntSize() = IntSize(width.roundToInt(), height.roundToInt())