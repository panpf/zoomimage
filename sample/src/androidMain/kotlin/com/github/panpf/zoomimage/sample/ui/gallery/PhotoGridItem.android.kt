package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.ui.examples.BasicPhotoGridItem
import com.github.panpf.zoomimage.sample.ui.examples.CoilPhotoGridItem
import com.github.panpf.zoomimage.sample.ui.examples.GlidePhotoGridItem
import com.github.panpf.zoomimage.sample.ui.examples.SketchPhotoGridItem
import com.github.panpf.zoomimage.sample.ui.model.Photo

@Composable
actual fun PhotoGridItem(
    index: Int,
    photo: Photo,
    staggeredGridMode: Boolean,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .let {
            val photoWidth = photo.width ?: 0
            val photoHeight = photo.height ?: 0
            if (staggeredGridMode && photoWidth > 0 && photoHeight > 0) {
                it.aspectRatio(photoWidth.toFloat() / photoHeight)
            } else {
                it.aspectRatio(1f)
            }
        }
    val appSettings = LocalPlatformContext.current.appSettings
    val composeImageLoader by appSettings.composeImageLoader.collectAsState()
    when (composeImageLoader) {
        "Sketch" -> SketchPhotoGridItem(index, photo, modifier, onClick)
        "Coil" -> CoilPhotoGridItem(index, photo, modifier, onClick)
        "Glide" -> GlidePhotoGridItem(index, photo, modifier, onClick)
        "Basic" -> BasicPhotoGridItem(index, photo, modifier, onClick)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}