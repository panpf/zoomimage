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

package com.github.panpf.zoomimage.subsampling

import androidx.annotation.MainThread
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.util.IntRectCompat

/**
 * Region decoding interface, used to decode the specified region of the image
 */
interface RegionDecoder : AutoCloseable {

    /**
     * The [SubsamplingImage] object that created this [RegionDecoder]
     */
    val subsamplingImage: SubsamplingImage

    /**
     * Image size, mimeType information
     */
    val imageInfo: ImageInfo

    /**
     * Make preparations before decoding, and more importantly, determine whether the current image is supported.
     */
    fun prepare()

    /**
     * Decode the specified region of the image
     */
    @WorkerThread
    fun decodeRegion(key: String, region: IntRectCompat, sampleSize: Int): TileImage

    /**
     * Copy a new [RegionDecoder] object
     */
    fun copy(): RegionDecoder

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String

    interface Factory {

        /**
         * Accept the [SubsamplingImage] object to be decoded, and return whether it is supported
         */
        @MainThread
        suspend fun accept(subsamplingImage: SubsamplingImage): Boolean

        /**
         * Check whether the specified mimeType is supported
         *
         * @return null: Uncertain, true: Supported, false: Not supported
         */
        fun checkSupport(mimeType: String): Boolean?

        fun create(
            subsamplingImage: SubsamplingImage,
            imageSource: ImageSource,
        ): RegionDecoder

        override fun equals(other: Any?): Boolean

        override fun hashCode(): Int

        override fun toString(): String
    }
}