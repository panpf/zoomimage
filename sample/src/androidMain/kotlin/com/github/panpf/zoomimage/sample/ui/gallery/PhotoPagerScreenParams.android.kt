package com.github.panpf.zoomimage.sample.ui.gallery

import android.os.Parcelable
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual data class PhotoPagerScreenParams actual constructor(
    actual val photos: List<Photo>,
    actual val totalCount: Int,
    actual val startPosition: Int,
    actual val initialPosition: Int
) : Parcelable, java.io.Serializable
// PhotoPagerParams as constructor parameters of PhotoPagerScreen
// voyager will serialize the constructor parameters of PhotoPagerScreen
// On Android, you need to implement the java.io.Serializable interface