package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PagerItem

@Composable
fun ExifOrientationTestScreen() {
    BaseScreen {
        ToolbarScaffold("Exif Orientation") {
            val pagerItems = remember {
                ComposeResImageFiles.exifs.map { imageFile ->
                    PagerItem(
                        data = imageFile,
                        titleFactory = { data ->
                            data.name.substring(0, data.name.indexOf(".")).uppercase()
                        },
                        contentFactory = { data, _, _ ->
                            SketchZoomAsyncImage(
                                uri = data.uri,
                                contentDescription = "view image",
                                modifier = Modifier.fillMaxSize(),
                                scrollBar = ScrollBarSpec.Medium.copy(
                                    windowInsets = WindowInsets.navigationBars
                                ),
                            )
                        }
                    )
                }.toTypedArray()
            }
            Box(Modifier.fillMaxSize()) {
                HorizontalTabPager(pagerItems = pagerItems)
            }
        }
    }
}