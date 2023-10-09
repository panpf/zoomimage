package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileSnapshot

interface TileBitmapDraw {

    fun ContentDrawScope.drawTile(
        imageInfo: ImageInfo,
        contentSize: IntSize,
        tileSnapshot: TileSnapshot
    ): Boolean
}