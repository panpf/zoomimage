package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Adds a capture ability on the Composable which can draw Bitmap from the Composable component.
 *
 * Example usage:
 *
 * ```
 *  val coroutineScope = rememberCoroutineScope()
 *  val capturableState = rememberCapturableState()
 *  Column(
 *      modifier = Modifier
 *          .capturable(capturableState)
 *          .clickable {
 *             coroutineScope.launch {
 *                 val imageBitmap = capturableState.capture()
 *                 // do something with the newly captured bitmap
 *             }
 *         }
 *  ) {
 *      // Composable content
 *  }
 * ```
 *
 * Ref: https://developer.android.com/develop/ui/compose/graphics/draw/modifiers#composable-to-bitmap
 */
fun Modifier.capturable(capturableState: CapturableState): Modifier {
    return this.drawWithContent {
        // call record to capture the content in the graphics layer
        capturableState.graphicsLayer.record {
            // draw the contents of the composable into the graphics layer
            this@drawWithContent.drawContent()
        }
        // draw the graphics layer on the visible canvas
        drawLayer(capturableState.graphicsLayer)
    }
}

/**
 * Creates [CapturableState] and remembers it.
 */
@Composable
fun rememberCapturableState(): CapturableState {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) { CapturableState(graphicsLayer) }
}

/**
 * Provides state for the Composable capture capability, provides a unique GraphicsLayer and obtains an ImageBitmap from the GraphicsLayer
 */
@Stable
data class CapturableState(internal val graphicsLayer: GraphicsLayer) {

    suspend fun capture(): ImageBitmap {
        return graphicsLayer.toImageBitmap()
    }
}