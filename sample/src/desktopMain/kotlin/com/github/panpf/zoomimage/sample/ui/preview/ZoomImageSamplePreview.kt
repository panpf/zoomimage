package com.github.panpf.zoomimage.sample.ui.preview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.sample.ui.examples.ZoomImageSample


@Preview
@Composable
private fun ZoomImageSamplePreview() {
    ZoomImageSample(ResourceImages.cat.uri)
}