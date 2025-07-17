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

package com.github.panpf.zoomimage.sketch

import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.toLogString

/**
 * [TileImage] implementation based on Sketch
 *
 * @see com.github.panpf.zoomimage.core.sketch3.test.SketchBitmapTileImageTest
 */
@Suppress("RedundantConstructorKeyword")
class SketchBitmapTileImage constructor(
    private val cacheValue: MemoryCache.Value,
) : BitmapTileImage(cacheValue.countBitmap.bitmap!!) {

    override fun setIsDisplayed(displayed: Boolean) {
        cacheValue.countBitmap.setIsDisplayed(displayed, "SketchTileImageCache")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SketchBitmapTileImage
        return cacheValue.countBitmap.bitmap == other.cacheValue.countBitmap.bitmap
    }

    override fun hashCode(): Int {
        return cacheValue.countBitmap.bitmap.hashCode()
    }

    override fun toString(): String {
        return "SketchBitmapTileImage(bitmap=${bitmap.toLogString()})"
    }
}