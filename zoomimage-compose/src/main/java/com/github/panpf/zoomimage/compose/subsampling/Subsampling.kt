package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import com.github.panpf.zoomimage.compose.zoom.ZoomableState

fun Modifier.subsampling(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState,
): Modifier = composed {
    this.drawWithContent {
        drawContent()

        // Trigger a refresh todo Verify that tilesChanged works
        @Suppress("UNUSED_VARIABLE") val changeCount = subsamplingState.tilesChanged

        subsamplingState.drawTiles(this, zoomableState.baseTransform)
    }
}