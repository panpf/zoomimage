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
class SketchBitmapTileImage(
    private val cacheValue: MemoryCache.Value,
    key: String,
    fromCache: Boolean,
    private val caller: String,
) : BitmapTileImage(cacheValue.countBitmap.bitmap!!, key, fromCache) {

    override fun setIsDisplayed(displayed: Boolean) {
        cacheValue.countBitmap.setIsDisplayed(displayed, caller)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SketchBitmapTileImage
        if (cacheValue.countBitmap.bitmap != other.cacheValue.countBitmap.bitmap) return false
        if (key != other.key) return false
        if (fromCache != other.fromCache) return false
        if (caller != other.caller) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cacheValue.countBitmap.bitmap.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + fromCache.hashCode()
        result = 31 * result + caller.hashCode()
        return result
    }

    override fun toString(): String {
        return "SketchBitmapTileImage(bitmap=${bitmap.toLogString()}, key='$key', fromCache=$fromCache, caller='$caller')"
    }
}