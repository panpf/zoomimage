package com.github.panpf.zoomimage.compose.glide

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntSize

/**
 * Return short string descriptions, for example: '100x200'
 */
@Stable
internal fun IntSize.toShortString(): String = "${width}x${height}"