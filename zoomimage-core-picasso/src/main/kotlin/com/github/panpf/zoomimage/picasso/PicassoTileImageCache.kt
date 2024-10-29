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

import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache
import com.squareup.picasso.Picasso
import com.squareup.picasso.internalCache

/**
 * Implement [TileImageCache] based on [Picasso]
 *
 * @see com.github.panpf.zoomimage.core.picasso.test.PicassoTileImageCacheTest
 */
class PicassoTileImageCache(private val picasso: Picasso) : TileImageCache {

    override fun get(key: String): TileImage? {
        val bitmap = picasso.internalCache[key] ?: return null
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
        picasso.internalCache.set(key, bitmap)
        return null
    }
}