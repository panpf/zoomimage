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
package com.github.panpf.zoomimage.internal

import android.graphics.Bitmap
import coil.ImageLoader
import coil.memory.MemoryCache
import com.github.panpf.zoomimage.core.CacheBitmap
import com.github.panpf.zoomimage.core.Size
import com.github.panpf.zoomimage.core.TinyMemoryCache

class CoilTinyMemoryCache(private val imageLoader: ImageLoader) : TinyMemoryCache {

    override fun get(key: String): CacheBitmap? {
        return imageLoader.memoryCache
            ?.get(MemoryCache.Key(key))
            ?.let { CoilCacheBitmap(key, it) }
    }

    override fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageSize: Size,
        imageMimeType: String,
        imageExifOrientation: Int,
        disallowReuseBitmap: Boolean
    ): CacheBitmap {
        val newCacheValue = MemoryCache.Value(bitmap)
        imageLoader.memoryCache?.set(MemoryCache.Key(key), newCacheValue)
        return CoilCacheBitmap(key, newCacheValue)
    }
}