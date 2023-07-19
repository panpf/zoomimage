package com.github.panpf.zoomimage.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import com.github.panpf.zoomimage.ZoomableState

fun Modifier.subsampling(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    this.drawWithContent {
        drawContent()

        // Trigger a refresh
        @Suppress("UNUSED_VARIABLE") val changeCount = subsamplingState.tilesChanged

        subsamplingState.drawTiles(this, zoomableState.baseTransform)
    }
}