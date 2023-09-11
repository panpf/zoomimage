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

package com.github.panpf.zoomimage.view.picasso.internal

import android.graphics.Bitmap
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.TileBitmapPoolHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.cache

class PicassoTileMemoryCache(private val picasso: Picasso) : TileMemoryCache {

    override fun get(key: String): TileBitmap? {
        return picasso.cache[key]
            ?.let { PicassoTileBitmap(key, it) }
    }

    override fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageInfo: ImageInfo,
        tileBitmapPoolHelper: TileBitmapPoolHelper
    ): TileBitmap {
        picasso.cache.set(key, bitmap)
        return PicassoTileBitmap(key, bitmap)
    }
}