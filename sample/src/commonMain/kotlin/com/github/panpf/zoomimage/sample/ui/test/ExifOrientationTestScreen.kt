package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PagerItem
import com.github.panpf.zoomimage.sample.ui.gallery.PhotoDetail

class ExifOrientationTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("ExifOrientation", ignoreNavigationBarInsets = true) {
            val colorScheme = MaterialTheme.colorScheme
            val pagerItems = remember {
                ResourceImages.exifs.map { imageFile ->
                    PagerItem(
                        data = imageFile,
                        titleFactory = { data ->
                            data.name
                        },
                        contentFactory = { data, _ ->
                            PhotoDetail(
                                sketchImageUri = data.uri,
                                photoPaletteState = remember {
                                    mutableStateOf(
                                        PhotoPalette(
                                            colorScheme
                                        )
                                    )
                                }
                            )
                        }
                    )
                }.toTypedArray()
            }
            HorizontalTabPager(pagerItems = pagerItems)
        }
    }
}