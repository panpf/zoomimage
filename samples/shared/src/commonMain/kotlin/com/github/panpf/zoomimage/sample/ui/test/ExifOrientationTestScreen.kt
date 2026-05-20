package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PagerItem
import com.github.panpf.zoomimage.sample.ui.examples.SketchZoomAsyncImageSample
import com.github.panpf.zoomimage.sample.ui.model.Photo

@Composable
fun ExifOrientationTestScreen() {
    BaseScreen {
        ToolbarScaffold("Exif Orientation") {
            val colorScheme = MaterialTheme.colorScheme
            val pagerItems = remember {
                ComposeResImageFiles.exifs.map { imageFile ->
                    PagerItem(
                        data = imageFile,
                        titleFactory = { data ->
                            data.name
                        },
                        contentFactory = { data, _, pageSelected ->
                            val photoPaletteState =
                                remember { mutableStateOf(PhotoPalette(colorScheme)) }
                            SketchZoomAsyncImageSample(
                                Photo(data.uri),
                                photoPaletteState,
                                pageSelected
                            )
                        }
                    )
                }.toTypedArray()
            }
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                HorizontalTabPager(pagerItems = pagerItems)
            }
        }
    }
}