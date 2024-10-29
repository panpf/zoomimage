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

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache

/**
 * Implement [TileImageCache] based on Sketch
 *
 * @see com.github.panpf.zoomimage.core.sketch3.test.SketchTileImageCacheTest
 */
class SketchTileImageCache(private val sketch: Sketch) : TileImageCache {

    override fun get(key: String): TileImage? {
        val cache = sketch.memoryCache[key] ?: return null
        return SketchBitmapTileImage(
            cacheValue = cache,
            key = key,
            fromCache = true,
            caller = "SketchTileImageCache",
        )
    }

    override fun put(
        key: String,
        tileImage: TileImage,
        imageUrl: String,
        imageInfo: com.github.panpf.zoomimage.subsampling.ImageInfo,
    ): TileImage? {
        val bitmap = (tileImage as BitmapTileImage).bitmap
        val newCountBitmap = CountBitmap(
            cacheKey = key,
            originBitmap = bitmap,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val newCacheValue = MemoryCache.Value(
            countBitmap = newCountBitmap,
            imageUri = imageUrl,
            requestKey = imageUrl,
            requestCacheKey = key,
            imageInfo = ImageInfo(
                imageInfo.width,
                imageInfo.height,
                imageInfo.mimeType,
                0
            ),
            transformedList = null,
            extras = null,
        )
        if (!sketch.memoryCache.put(key, newCacheValue)) {
            return null
        }
        return SketchBitmapTileImage(
            cacheValue = newCacheValue,
            key = tileImage.key,
            fromCache = tileImage.fromCache,
            caller = "SketchTileImageCache"
        )
    }
}