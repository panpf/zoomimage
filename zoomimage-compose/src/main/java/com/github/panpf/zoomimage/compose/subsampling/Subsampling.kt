package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import com.github.panpf.zoomimage.compose.zoom.ZoomableState

fun Modifier.subsampling(
    subsamplingState: SubsamplingState,
    zoomableState: ZoomableState?,
): Modifier = this.drawWithContent {
    drawContent()
    subsamplingState.tilesChanged * 1 // Trigger a refresh
    subsamplingState.drawTiles(this, zoomableState?.baseTransform)
}