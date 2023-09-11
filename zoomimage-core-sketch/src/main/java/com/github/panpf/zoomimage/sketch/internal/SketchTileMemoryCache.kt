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

package com.github.panpf.zoomimage.sketch.internal

import android.graphics.Bitmap
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.TileBitmapPoolHelper

class SketchTileMemoryCache constructor(
    private val sketch: Sketch,
    private val caller: String
) : TileMemoryCache {

    override fun get(key: String): TileBitmap? {
        return sketch.memoryCache[key]?.let {
            SketchTileBitmap(key = key, cacheValue = it, caller = caller)
        }
    }

    override fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: com.github.panpf.zoomimage.subsampling.ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap {
        val newCountBitmap = CountBitmap(
            cacheKey = key,
            originBitmap = bitmap,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = tileBitmapPoolHelper.disallowReuseBitmap,
        )
        val newCacheValue = MemoryCache.Value(
            countBitmap = newCountBitmap,
            imageUri = imageKey,
            requestKey = imageKey,
            requestCacheKey = key,
            imageInfo = ImageInfo(
                imageInfo.width,
                imageInfo.height,
                imageInfo.mimeType,
                imageInfo.exifOrientation
            ),
            transformedList = null,
            extras = null,
        )
        sketch.memoryCache.put(key, newCacheValue)
        return SketchTileBitmap(key, newCacheValue, caller)
    }
}