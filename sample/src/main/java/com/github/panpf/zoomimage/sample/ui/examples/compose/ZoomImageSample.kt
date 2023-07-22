package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.rememberLogger
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sketch.internal.SketchImageSource
import com.github.panpf.zoomimage.sketch.internal.SketchTileBitmapPool
import com.github.panpf.zoomimage.sketch.internal.SketchTileMemoryCache
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun ZoomImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        logger = rememberLogger(),
        sketchImageUri = sketchImageUri,
        supportIgnoreExifOrientation = true
    ) { contentScale, alignment, zoomableState, subsamplingState, ignoreExifOrientation, scrollBarSpec, onLongPress ->
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            subsamplingState.tileBitmapPool = SketchTileBitmapPool(context.sketch, "ZoomImage")
            subsamplingState.tileMemoryCache = SketchTileMemoryCache(context.sketch, "ZoomImage")
        }
        LaunchedEffect(ignoreExifOrientation) {
            subsamplingState.ignoreExifOrientation = ignoreExifOrientation
        }

        var drawablePainter: Painter? by remember { mutableStateOf(null) }
        LaunchedEffect(sketchImageUri, ignoreExifOrientation) {
            val drawable = DisplayRequest(context, sketchImageUri) {
                crossfade()
                ignoreExifOrientation(ignoreExifOrientation)
            }.execute().drawable
            drawablePainter = drawable?.let { DrawablePainter(it) }

            val imageSource = SketchImageSource(context, context.sketch, sketchImageUri)
            subsamplingState.setImageSource(imageSource)
        }

        val drawablePainter1 = drawablePainter
        if (drawablePainter1 != null) {
            ZoomImage(
                painter = drawablePainter1,
                contentDescription = "ZoomImage",
                contentScale = contentScale,
                alignment = alignment,
                modifier = Modifier.fillMaxSize(),
                zoomableState = zoomableState,
                subsamplingState = subsamplingState,
                scrollBarSpec = scrollBarSpec,
                onLongPress = onLongPress,
            )
        }
    }
}

@Preview
@Composable
private fun ZoomImageSamplePreview() {
    ZoomImageSample(newResourceUri(R.drawable.im_placeholder))
}
