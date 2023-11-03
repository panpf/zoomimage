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

package com.github.panpf.zoomimage.subsampling.internal

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat

/**
 * Decode the tile bitmap of the image
 */
interface TileDecoder {

    val imageInfo: ImageInfo

    val exifOrientation: ExifOrientation?

    @WorkerThread
    fun decode(srcRect: IntRectCompat, sampleSize: Int): TileBitmap?

    @MainThread
    fun destroy(caller: String)
}