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
package com.github.panpf.zoomimage.coil.internal

import android.graphics.Bitmap
import coil.ImageLoader
import coil.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper

class CoilTileMemoryCache(private val imageLoader: ImageLoader) : TileMemoryCache {

    override fun get(key: String): TileBitmap? {
        return imageLoader.memoryCache
            ?.get(MemoryCache.Key(key))
            ?.let { CoilTileBitmap(key, it) }
    }

    override fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap {
        val newCacheValue = MemoryCache.Value(bitmap)
        imageLoader.memoryCache?.set(MemoryCache.Key(key), newCacheValue)
        return CoilTileBitmap(key, newCacheValue)
    }
}