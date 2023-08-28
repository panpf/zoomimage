package com.github.panpf.zoomimage.compose.subsampling

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntRect
import com.github.panpf.zoomimage.subsampling.Tile.State

/**
 * A snapshot of the tile
 */
@Immutable
data class TileSnapshot(
    /**
     * The region of Tile in the original image
     */
    val srcRect: IntRect,

    /**
     * The sampling multiplier at load
     */
    val inSampleSize: Int,

    /**
     * The bitmap of the tile
     */
    val bitmap: Bitmap?,

    /**
     * The state of the tile
     */
    @State
    val state: Int,
)