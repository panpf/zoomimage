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
import androidx.annotation.IntDef
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.internal.toShortString
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.Job

/**
 * A tile of the image, store the region, sample multiplier, Bitmap, load status, and other information of the tile
 */
class Tile constructor(

    /**
     * The region of Tile in the original image
     */
    val srcRect: IntRectCompat,

    /**
     * The sampling multiplier at load
     */
    val inSampleSize: Int
) {

    internal var tileBitmap: TileBitmap? = null
        set(value) {
            field?.setIsDisplayed(false)
            field = value
            value?.setIsDisplayed(true)
        }
    internal var loadJob: Job? = null

    /**
     * The bitmap of the tile
     */
    val bitmap: Bitmap?
        get() = tileBitmap?.bitmap

    /**
     * The state of the tile
     */
    @State
    var state: Int = STATE_NONE

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

    companion object {
        const val STATE_NONE = 0
        const val STATE_LOADING = 1
        const val STATE_LOADED = 2
        const val STATE_ERROR = 3
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(STATE_NONE, STATE_LOADING, STATE_LOADED, STATE_ERROR)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
    annotation class State
}