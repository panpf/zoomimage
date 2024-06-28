package com.github.panpf.zoomimage.sample.ui.test.compose

import androidx.compose.runtime.Composable
import com.githb.panpf.zoomimage.images.Images
import com.github.panpf.zoomimage.sample.ui.base.compose.BaseAppBarComposeFragment
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageSample

class ZoomImageTestFragment : BaseAppBarComposeFragment() {

    override fun getTitle(): String = "ZoomImage Test"

    @Composable
    override fun DrawContent() {
        ZoomImageSample(sketchImageUri = Images.hugeLongQmsht.uri)
    }
}