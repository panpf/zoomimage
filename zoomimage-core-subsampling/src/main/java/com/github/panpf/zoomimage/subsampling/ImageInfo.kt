/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.IntSizeCompat

/**
 * Image information
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.ImageInfoTest]
 */
data class ImageInfo constructor(
    /**
     * Image size
     */
    val size: IntSizeCompat,

    /**
     * Image mime type
     */
    val mimeType: String,

    /**
     * Image exif orientation
     */
    val exifOrientation: Int,
) {

    /**
     * Create a new [ImageInfo] based on the specified parameters
     *
     * @param width Image width
     * @param height Image height
     * @param mimeType Image mime type
     * @param exifOrientation Image exif orientation
     */
    constructor(
        width: Int,
        height: Int,
        mimeType: String,
        exifOrientation: Int,
    ) : this(IntSizeCompat(width, height), mimeType, exifOrientation)

    /**
     * Image width
     */
    val width: Int = size.width

    /**
     * Image height
     */
    val height: Int = size.height

    /**
     * Create a new [ImageInfo] based on the current [ImageInfo]
     */
    fun copy(
        width: Int = this.width,
        height: Int = this.height,
        mimeType: String = this.mimeType,
        exifOrientation: Int = this.exifOrientation,
    ): ImageInfo = ImageInfo(width, height, mimeType, exifOrientation)

    override fun toString(): String {
        val exifOrientationName = exifOrientationName(exifOrientation)
        return "ImageInfo(size=${width}x$height, mimeType='$mimeType', exifOrientation=$exifOrientationName)"
    }

    fun toShortString(): String =
        "(${width}x$height,'$mimeType',${exifOrientationName(exifOrientation)})"
}