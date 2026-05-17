package com.github.panpf.zoomimage.sample.ui.gallery

import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.serialization.Serializable

@Serializable
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual data class PhotoPagerScreenParams actual constructor(
    actual val photos: List<Photo>,
    actual val totalCount: Int,
    actual val startPosition: Int,
    actual val initialPosition: Int
)