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

package com.github.panpf.zoomimage.coil

import coil.ImageLoader
import coil.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

/**
 * Implement [TileBitmapCache] based on Coil
 *
 * @see com.github.panpf.zoomimage.core.coil2.test.CoilTileBitmapCacheTest
 */
class CoilTileBitmapCache(private val imageLoader: ImageLoader) : TileBitmapCache {

    override fun get(key: String): TileBitmap? {
        val cacheValue = imageLoader.memoryCache?.get(MemoryCache.Key(key)) ?: return null
        val bitmap = cacheValue.bitmap
        return AndroidTileBitmap(bitmap, key, BitmapFrom.MEMORY_CACHE)
    }

    override fun put(
        key: String,
        tileBitmap: TileBitmap,
        imageUrl: String,
        imageInfo: ImageInfo,
    ): TileBitmap? {
        tileBitmap as AndroidTileBitmap
        val bitmap = tileBitmap.bitmap!!
        val memoryCache = imageLoader.memoryCache
        memoryCache?.set(MemoryCache.Key(key), MemoryCache.Value(bitmap))
        return null
    }
}