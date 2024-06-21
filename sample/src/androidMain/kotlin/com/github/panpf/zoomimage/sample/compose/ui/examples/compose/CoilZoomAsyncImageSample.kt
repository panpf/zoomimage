package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import coil.request.ImageRequest.Builder
import coil.size.Precision.INEXACT
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.tools4a.toast.ktx.showShortToast
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.util.compose.toShortString
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

@Composable
fun CoilZoomAsyncImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
    ) { contentScale, alignment, state: ZoomState, scrollBar ->
        var myLoadState by remember { mutableStateOf<MyLoadState>(MyLoadState.None) }
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val request = remember(key1 = sketchImageUri) {
            val model = sketchUri2CoilModel(context, sketchImageUri)
            Builder(context).apply {
                lifecycle(lifecycle)
                precision(INEXACT)
                data(model)
                crossfade(true)
                listener(
                    onStart = {
                        myLoadState = MyLoadState.Loading
                    },
                    onError = { _, _ ->
                        myLoadState = MyLoadState.Error()
                    },
                    onSuccess = { _, _ ->
                        myLoadState = MyLoadState.None
                    }
                )
            }.build()
        }
        CoilZoomAsyncImage(
            model = request,
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            state = state,
            scrollBar = scrollBar,
            onTap = {
                context.showShortToast("Click (${it.toShortString()}")
            },
            onLongPress = {
                context.showShortToast("Long click (${it.toShortString()})")
            }
        )

        LoadState(loadState = myLoadState)
    }
}

@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    CoilZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}