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
import com.github.panpf.zoomimage.Logger

class TileMemoryCacheHelper(@Suppress("UNUSED_PARAMETER") logger: Logger) {

    var tileMemoryCache: TileMemoryCache? = null
    var disableMemoryCache: Boolean = false

    /**
     * Get the cache of the key
     */
    fun get(key: String): TileBitmap? {
        val tileMemoryCache = tileMemoryCache
        val disableMemoryCache = disableMemoryCache
        if (tileMemoryCache == null || disableMemoryCache) {
            return null
        }
        return tileMemoryCache.get(key)
    }

    fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap {
        val tileMemoryCache = tileMemoryCache
        val disableMemoryCache = disableMemoryCache
        if (tileMemoryCache == null || disableMemoryCache) {
            return DefaultTileBitmap(key, bitmap)
        }
        return tileMemoryCache.put(key, bitmap, imageKey, imageInfo, tileBitmapPoolHelper)
            ?: DefaultTileBitmap(key, bitmap)
    }
}