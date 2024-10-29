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
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache

/**
 * Implement [TileImageCache] based on Coil
 *
 * @see com.github.panpf.zoomimage.core.coil2.test.CoilTileImageCacheTest
 */
class CoilTileImageCache(private val imageLoader: ImageLoader) : TileImageCache {

    override fun get(key: String): TileImage? {
        val cacheValue = imageLoader.memoryCache?.get(MemoryCache.Key(key)) ?: return null
        val bitmap = cacheValue.bitmap
        return BitmapTileImage(bitmap, key, fromCache = true)
    }

    override fun put(
        key: String,
        tileImage: TileImage,
        imageUrl: String,
        imageInfo: ImageInfo,
    ): TileImage? {
        tileImage as BitmapTileImage
        val bitmap = tileImage.bitmap
        val memoryCache = imageLoader.memoryCache
        memoryCache?.set(MemoryCache.Key(key), MemoryCache.Value(bitmap))
        return null
    }
}