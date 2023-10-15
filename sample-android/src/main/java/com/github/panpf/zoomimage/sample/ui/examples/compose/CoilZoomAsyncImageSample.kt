package com.github.panpf.zoomimage.sample.ui.examples.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.request.ImageRequest
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

// todo API 21 的模拟器上从大图页面返回后崩溃，貌似是清明上河图图片，coil 加载的太大了
@Composable
fun CoilZoomAsyncImageSample(sketchImageUri: String) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
        supportIgnoreExifOrientation = false
    ) { contentScale, alignment, state: ZoomState, _, scrollBar ->
        val context = LocalContext.current
        val coilData =
            remember(key1 = sketchImageUri) { sketchUri2CoilModel(context, sketchImageUri) }
        CoilZoomAsyncImage(
            model = ImageRequest.Builder(LocalContext.current).apply {
                data(coilData)
                crossfade(true)
            }.build(),
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            state = state,
            scrollBar = scrollBar,
        )
    }
}

@Preview
@Composable
private fun CoilZoomAsyncImageSamplePreview() {
    CoilZoomAsyncImageSample(newResourceUri(R.drawable.im_placeholder))
}