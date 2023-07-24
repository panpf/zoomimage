package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import com.github.panpf.zoomimage.compose.zoom.ZoomableState

fun Modifier.subsampling(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    subsamplingState.tilesChanged * 1 // Trigger a refresh
    val baseTransform = zoomableState.baseTransform
    this.drawWithContent {
        drawContent()
        subsamplingState.drawTiles(this, baseTransform)
    }
}