package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import coil.request.ImageRequest
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
        supportIgnoreExifOrientation = false
    ) { contentScale, alignment, state: ZoomState, _, scrollBar ->
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val coilData =
            remember(key1 = sketchImageUri) { sketchUri2CoilModel(context, sketchImageUri) }
        CoilZoomAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).apply {
                lifecycle(lifecycle)
                precision(coil.size.Precision.INEXACT)
                data(coilData)
                crossfade(true)
//                val imageLoader = Coil.imageLoader(context)
//                if (coilData != null) {
//                    val key = imageLoader.components.key(coilData, Options(context))
//                    placeholderMemoryCacheKey(key)
//                }
            }.build(),
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
    }
}

@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    CoilZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}