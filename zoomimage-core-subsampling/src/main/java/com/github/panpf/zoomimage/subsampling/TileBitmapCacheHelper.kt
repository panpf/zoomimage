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

import com.github.panpf.zoomimage.Logger

/**
 * Assist [TileManager] to obtain and store Bitmap from [TileBitmapCache]
 */
class TileBitmapCacheHelper constructor(
    @Suppress("UNUSED_PARAMETER") logger: Logger,
    private val tileBitmapCacheSpec: TileBitmapCacheSpec
) {

    fun get(key: String): CacheTileBitmap? {
        val disabled = tileBitmapCacheSpec.disabled
        val tileMemoryCache = tileBitmapCacheSpec.tileBitmapCache
        if (disabled || tileMemoryCache == null) {
            return null
        }
        return tileMemoryCache.get(key)
    }

    fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: ImageInfo,
        disallowReuseBitmap: Boolean
    ): CacheTileBitmap? {
        val disabled = tileBitmapCacheSpec.disabled
        val tileMemoryCache = tileBitmapCacheSpec.tileBitmapCache
        if (disabled || tileMemoryCache == null) {
            return null
        }
        return tileMemoryCache.put(key, tileBitmap, imageUrl, imageInfo, disallowReuseBitmap)
    }
}