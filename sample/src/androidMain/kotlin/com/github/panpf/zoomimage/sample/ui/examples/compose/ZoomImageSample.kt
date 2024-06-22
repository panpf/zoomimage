package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.sketch.sketch
import com.github.panpf.tools4a.toast.ktx.showShortToast
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.toShortString
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.sketch.SketchTileBitmapCache
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun ZoomImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
    ) { contentScale, alignment, state, scrollBar ->
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            state.subsampling.tileBitmapCache = SketchTileBitmapCache(context.sketch, "ZoomImage")
        }

        var myLoadState by remember { mutableStateOf<MyLoadState>(MyLoadState.None) }
        var drawablePainter: DrawablePainter? by remember { mutableStateOf(null) }
        LaunchedEffect(sketchImageUri) {
            myLoadState = MyLoadState.Loading
            val displayResult = DisplayRequest(context, sketchImageUri).execute()
            myLoadState = if (displayResult is DisplayResult.Success) {
                MyLoadState.None
            } else {
                MyLoadState.Error()
            }
            val drawable = displayResult.drawable
            drawablePainter = drawable?.let { DrawablePainter(it) }

            val imageSource = SketchImageSource(context, context.sketch, sketchImageUri)
            state.subsampling.setImageSource(imageSource)
        }

        val drawablePainter1 = drawablePainter
        if (drawablePainter1 != null) {
            ZoomImage(
                painter = drawablePainter1,
                contentDescription = "view image",
                contentScale = contentScale,
                alignment = alignment,
                modifier = Modifier.fillMaxSize(),
                state = state,
                scrollBar = scrollBar,
                onTap = {
                    context.showShortToast("Click (${it.toShortString()})")
                },
                onLongPress = {
                    context.showShortToast("Long click (${it.toShortString()})")
                }
            )
        }

        LoadState(loadState = myLoadState)
    }
}

@Preview
@Composable
private fun ZoomImageSamplePreview() {
    ZoomImageSample(newResourceUri(R.drawable.im_placeholder))
}
