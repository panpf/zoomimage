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

package com.github.panpf.zoomimage.picasso

import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmapCache
import com.squareup.picasso.Picasso
import com.squareup.picasso.internalCache

/**
 * Implement [TileBitmapCache] based on [Picasso]
 *
 * @see com.github.panpf.zoomimage.core.picasso.test.PicassoTileBitmapCacheTest
 */
class PicassoTileBitmapCache(private val picasso: Picasso) : TileBitmapCache {

    override fun get(key: String): TileBitmap? {
        val bitmap = picasso.internalCache[key] ?: return null
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
        picasso.internalCache.set(key, bitmap)
        return null
    }
}