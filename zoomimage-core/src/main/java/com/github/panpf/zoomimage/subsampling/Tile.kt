/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.internal.toShortString
import com.github.panpf.zoomimage.core.toShortString
import kotlinx.coroutines.Job

class Tile constructor(val srcRect: IntRectCompat, val inSampleSize: Int) {

    internal var tileBitmap: TileBitmap? = null
        set(value) {
            field?.setIsDisplayed(false)
            field = value
            value?.setIsDisplayed(true)
        }

    val bitmap: Bitmap?
        get() = tileBitmap?.bitmap
    var loadJob: Job? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Tile
        if (srcRect != other.srcRect) return false
        if (inSampleSize != other.inSampleSize) return false
        if (bitmap != other.bitmap) return false
        return true
    }

    override fun hashCode(): Int {
        var result = srcRect.hashCode()
        result = 31 * result + inSampleSize
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Tile(" +
                "srcRect=${srcRect.toShortString()}," +
                "inSampleSize=$inSampleSize," +
                "bitmap=${bitmap?.toShortString().orEmpty()})"
    }
}