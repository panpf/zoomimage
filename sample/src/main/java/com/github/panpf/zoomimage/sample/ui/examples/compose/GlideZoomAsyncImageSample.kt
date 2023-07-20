package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.rememberGlideZoomAsyncImageLogger
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideZoomAsyncImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        logger = rememberGlideZoomAsyncImageLogger(),
        sketchImageUri = sketchImageUri,
        supportIgnoreExifOrientation = false
    ) { contentScale, alignment, zoomableState, subsamplingState, _, scrollBarSpec, onLongPress ->
        val glideData =
            remember(key1 = sketchImageUri) { sketchUri2GlideModel(sketchImageUri) }
        GlideZoomAsyncImage(
            model = glideData,
            contentDescription = "GlideZoomAsyncImage",
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

@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    CoilZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}