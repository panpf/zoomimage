package com.github.panpf.zoomimage.view.subsampling

import android.graphics.Bitmap
import com.github.panpf.zoomimage.subsampling.Tile.State
import com.github.panpf.zoomimage.util.IntRectCompat

data class TileSnapshot(
    val srcRect: IntRectCompat,
    val inSampleSize: Int,
    val bitmap: Bitmap?,
    @State val state: Int,
)