package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.github.panpf.sketch.fetch.newComposeResourceUri
import com.github.panpf.zoomimage.sample.ui.gallery.SketchZoomAsyncImageSample


@Preview
@Composable
private fun SketchZoomAsyncImageSamplePreview() {
    SketchZoomAsyncImageSample(newComposeResourceUri("files/huge_china.jpg"))
}