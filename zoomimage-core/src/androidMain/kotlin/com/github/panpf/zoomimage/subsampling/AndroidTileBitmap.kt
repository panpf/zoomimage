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

import android.graphics.Bitmap
import com.github.panpf.zoomimage.util.toLogString

/**
 * @see [com.github.panpf.zoomimage.core.android.test.subsampling.AndroidTileBitmapTest]
 */
class AndroidTileBitmap constructor(
    val bitmap: Bitmap,
    override val key: String,
    override val bitmapFrom: BitmapFrom,
) : TileBitmap {

    override val width: Int = bitmap.width

    override val height: Int = bitmap.height

    override val byteCount: Long = bitmap.byteCount.toLong()

    override val isRecycled: Boolean
        get() = bitmap.isRecycled

    override fun setIsDisplayed(displayed: Boolean) {

    }

    override fun recycle() {
        bitmap.recycle()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as AndroidTileBitmap
        if (bitmap != other.bitmap) return false
        if (key != other.key) return false
        if (bitmapFrom != other.bitmapFrom) return false
        return true
    }

    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + bitmapFrom.hashCode()
        return result
    }

    override fun toString(): String {
        return "AndroidTileBitmap(key='$key', bitmap=${bitmap.toLogString()}, bitmapFrom=$bitmapFrom)"
    }
}