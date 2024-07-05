package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PagerItem
import com.github.panpf.zoomimage.sample.ui.examples.ZoomImageSample

class ExifOrientationTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("ExifOrientation", ignoreNavigationBarInsets = true) {
            val pagerItems = remember {
                ResourceImages.exifs.map { imageFile ->
                    PagerItem(
                        data = imageFile,
                        titleFactory = { data ->
                            data.name
                        },
                        contentFactory = { data, _ ->
                            ZoomImageSample(sketchImageUri = data.uri)
                        }
                    )
                }.toTypedArray()
            }
            HorizontalTabPager(pagerItems = pagerItems)
        }
    }
}