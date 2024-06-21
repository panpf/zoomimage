package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.panpf.sketch.compose.ability.progressIndicator
import com.github.panpf.sketch.compose.ability.rememberDrawableProgressPainter
import com.github.panpf.sketch.compose.rememberAsyncImageState
import com.github.panpf.sketch.drawable.SectorProgressDrawable
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.LoadState
import com.github.panpf.sketch.stateimage.ThumbnailMemoryCacheStateImage
import com.github.panpf.tools4a.toast.ktx.showShortToast
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString

@Composable
fun SketchZoomAsyncImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
    ) { contentScale, alignment, state, scrollBar ->
        val context = LocalContext.current
        val imageState = rememberAsyncImageState()
        val progressPainter =
            rememberDrawableProgressPainter(drawable = remember { SectorProgressDrawable() })
        SketchZoomAsyncImage(
            request = DisplayRequest(context, sketchImageUri) {
                placeholder(ThumbnailMemoryCacheStateImage())
                crossfade(fadeStart = false)
            },
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier
                .fillMaxSize()
                .progressIndicator(imageState, progressPainter),
            imageState = imageState,
            state = state,
            scrollBar = scrollBar,
            onTap = {
                context.showShortToast("Click (${it.toShortString()})")
            },
            onLongPress = {
                context.showShortToast("Long click (${it.toShortString()})")
            }
        )

        val myLoadState by remember {
            derivedStateOf {
                if (imageState.loadState is LoadState.Error) {
                    MyLoadState.Error { imageState.restart() }
                } else MyLoadState.None
            }
        }
        LoadState(loadState = myLoadState)
    }
}

@Preview
@Composable
private fun SketchZoomAsyncImageSamplePreview() {
    SketchZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}