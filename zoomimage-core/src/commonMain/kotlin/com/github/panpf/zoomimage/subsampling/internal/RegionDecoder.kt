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

import androidx.annotation.MainThread
import com.github.panpf.zoomimage.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.util.IntRectCompat

interface RegionDecoder : AutoCloseable {

    val imageSource: ImageSource

    val imageInfo: ImageInfo

    @WorkerThread
    fun decodeRegion(key: String, region: IntRectCompat, sampleSize: Int): TileImage

    fun copy(): RegionDecoder

    interface Matcher {

        @MainThread
        suspend fun accept(subsamplingImage: SubsamplingImage): Factory?
    }

    interface Factory : AutoCloseable {

        @WorkerThread
        suspend fun decodeImageInfo(imageSource: ImageSource): ImageInfo

        @MainThread
        fun checkSupport(mimeType: String): Boolean?

        @WorkerThread
        suspend fun create(imageSource: ImageSource, imageInfo: ImageInfo): RegionDecoder
    }
}