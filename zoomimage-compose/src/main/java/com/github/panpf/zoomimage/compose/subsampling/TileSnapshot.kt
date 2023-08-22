package com.github.panpf.zoomimage.compose.subsampling

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntRect
import com.github.panpf.zoomimage.subsampling.Tile.State

@Immutable
data class TileSnapshot(
    val srcRect: IntRect,
    val inSampleSize: Int,
    val bitmap: Bitmap?,
    @State val state: Int,
)