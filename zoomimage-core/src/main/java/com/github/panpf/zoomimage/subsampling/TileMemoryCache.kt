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
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper

/**
 * Tile memory cache container interface, you need to implement it to customize tile memory cache
 */
interface TileMemoryCache {

    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap?

    /**
     * Put the cache of the key
     *
     * @param key Cache key
     * @param bitmap Tile bitmap
     * @param imageKey Image key
     * @param imageInfo Image information
     * @param tileBitmapPoolHelper The helper class used to access [TileBitmapPool]
     */
    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap?
}

/**
 * Tile bitmap interface, you need to implement it to customize tile bitmap
 */
interface TileBitmap {

    /**
     * Cache key
     */
    val key: String

    /**
     * Tile bitmap
     */
    val bitmap: Bitmap?

    /**
     * TileBitmap displays callbacks when the state changes
     */
    fun setIsDisplayed(displayed: Boolean)
}

class DefaultTileBitmap(override val key: String, override val bitmap: Bitmap?) : TileBitmap {

    override fun setIsDisplayed(displayed: Boolean) {

    }
}