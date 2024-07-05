package com.github.panpf.zoomimage.sample.ui.gallery

import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.serialization.Serializable

@Serializable
actual data class PhotoPagerScreenParams actual constructor(
    actual val photos: List<Photo>,
    actual val startPosition: Int,
    actual val initialPosition: Int
)