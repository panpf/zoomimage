package com.github.panpf.zoomimage.sample.ui.gallery

import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.serialization.Serializable

@Serializable
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhotoPagerScreenParams {

    val photos: List<Photo>
    val totalCount: Int
    val startPosition: Int
    val initialPosition: Int

    constructor(
        photos: List<Photo>,
        totalCount: Int,
        startPosition: Int,
        initialPosition: Int
    )
}

fun buildPhotoPagerScreenParams(
    items: List<Photo>,
    position: Int
): PhotoPagerScreenParams {
    val totalCount = items.size
    val startPosition = (position - 100).coerceAtLeast(0)
    val endPosition = (position + 100).coerceAtMost(items.size - 1)
    val photos = items.asSequence()
        .filterIndexed { index, _ -> index in startPosition..endPosition }
        .toList()
    return PhotoPagerScreenParams(
        photos = photos,
        totalCount = totalCount,
        startPosition = startPosition,
        initialPosition = position
    )
}