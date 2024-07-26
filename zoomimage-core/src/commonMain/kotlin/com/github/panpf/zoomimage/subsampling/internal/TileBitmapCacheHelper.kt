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

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec

/**
 * Assist [TileManager] to obtain and store Bitmap from [TileBitmapCache]
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.internal.TileBitmapCacheHelperTest
 */
class TileBitmapCacheHelper constructor(
    val tileBitmapCacheSpec: TileBitmapCacheSpec
) {

    fun get(key: String): TileBitmap? {
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
    ): TileBitmap? {
        val disabled = tileBitmapCacheSpec.disabled
        val tileMemoryCache = tileBitmapCacheSpec.tileBitmapCache
        return if (!disabled && tileMemoryCache != null) {
            tileMemoryCache.put(
                key = key,
                tileBitmap = tileBitmap,
                imageUrl = imageUrl,
                imageInfo = imageInfo
            )
        } else {
            null
        }
    }
}