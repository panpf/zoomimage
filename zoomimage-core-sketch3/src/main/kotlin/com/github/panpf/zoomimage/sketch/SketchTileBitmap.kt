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

package com.github.panpf.zoomimage.sketch

import android.graphics.Bitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.zoomimage.sketch.internal.toLogString
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.TileBitmap

/**
 * [TileBitmap] implementation based on Sketch
 *
 * @see com.github.panpf.zoomimage.core.sketch3.test.SketchTileBitmapTest
 */
class SketchTileBitmap constructor(
    private val cacheValue: MemoryCache.Value,
    override val key: String,
    override val bitmapFrom: BitmapFrom,
    private val caller: String,
) : AndroidTileBitmap {

    override val bitmap: Bitmap?
        get() = cacheValue.countBitmap.bitmap

    override val width: Int = bitmap!!.width

    override val height: Int = bitmap!!.height

    override val byteCount: Long = bitmap!!.byteCount.toLong()

    override val isRecycled: Boolean
        get() = bitmap?.isRecycled ?: true

    private val toString by lazy {
        "SketchTileBitmap(key='$key', bitmap=${bitmap!!.toLogString()}, bitmapFrom=$bitmapFrom)"
    }

    override fun recycle() {
        bitmap?.recycle()
    }

    override fun setIsDisplayed(displayed: Boolean) {
        cacheValue.countBitmap.setIsDisplayed(displayed, caller)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SketchTileBitmap
        if (bitmap != other.bitmap) return false
        if (key != other.key) return false
        if (bitmapFrom != other.bitmapFrom) return false
        if (caller != other.caller) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cacheValue.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + bitmapFrom.hashCode()
        result = 31 * result + caller.hashCode()
        return result
    }

    override fun toString(): String {
        return toString
    }
}