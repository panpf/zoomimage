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
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.components.HorizontalTabPager
import com.github.panpf.zoomimage.sample.ui.components.PagerItem
import com.github.panpf.zoomimage.sample.ui.gallery.PhotoDetail
import com.github.panpf.zoomimage.sample.ui.model.Photo

class ExifOrientationTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("Exif Orientation", ignoreNavigationBarInsets = true) {
            val colorScheme = MaterialTheme.colorScheme
            val pagerItems = remember {
                ResourceImages.exifs.map { imageFile ->
                    PagerItem(
                        data = imageFile,
                        titleFactory = { data ->
                            data.name
                        },
                        contentFactory = { data, _, pageSelected ->
                            val photoPaletteState =
                                remember { mutableStateOf(PhotoPalette(colorScheme)) }
                            PhotoDetail(Photo(data.uri), photoPaletteState, pageSelected)
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