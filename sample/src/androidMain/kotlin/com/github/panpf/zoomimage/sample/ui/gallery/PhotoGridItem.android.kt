package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.ui.examples.CoilPhotoGridItem
import com.github.panpf.zoomimage.sample.ui.examples.GlidePhotoGridItem
import com.github.panpf.zoomimage.sample.ui.examples.SketchPhotoGridItem
import com.github.panpf.zoomimage.sample.ui.model.Photo

@Composable
actual fun PhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val composeImageLoader by LocalPlatformContext.current.appSettings.composeImageLoader.collectAsState()
    when (composeImageLoader) {
        "Sketch" -> SketchPhotoGridItem(index, photo, modifier, onClick)
        "Coil" -> CoilPhotoGridItem(index, photo, modifier, onClick)
        "Glide" -> GlidePhotoGridItem(index, photo, modifier, onClick)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}