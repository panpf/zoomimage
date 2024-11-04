/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.util.IntSizeCompat
import okio.buffer
import okio.use

/**
 * Get the platform's default RegionDecoder
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDefaultRegionDecoder
 */
actual fun defaultRegionDecoder(): RegionDecoder.Factory {
    return AndroidRegionDecoder.Factory()
}

/**
 * Decode the Exif orientation of the image
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDecodeExifOrientation
 */
@WorkerThread
internal fun ImageSource.decodeExifOrientation(): Int {
    val exifOrientation = openSource().buffer().inputStream().use {
        ExifInterface(it).getAttributeInt(
            /* tag = */ ExifInterface.TAG_ORIENTATION,
            /* defaultValue = */ ExifInterface.ORIENTATION_UNDEFINED
        )
    }
    return exifOrientation
}


/**
 * Decode the image width and height and mimeType
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.internal.DecodesAndroidTest.testDecodeImageInfo
 */
internal fun ImageSource.decodeImageInfo(): ImageInfo {
    val boundOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    openSource().buffer().inputStream().use {
        BitmapFactory.decodeStream(it, null, boundOptions)
    }
    val mimeType = boundOptions.outMimeType.orEmpty()
    val imageSize = IntSizeCompat(
        width = boundOptions.outWidth,
        height = boundOptions.outHeight
    )
    return ImageInfo(size = imageSize, mimeType = mimeType)
}