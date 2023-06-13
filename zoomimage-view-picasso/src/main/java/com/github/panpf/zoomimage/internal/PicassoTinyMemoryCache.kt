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
import com.github.panpf.zoomimage.core.CacheBitmap
import com.github.panpf.zoomimage.core.Size
import com.github.panpf.zoomimage.core.TinyMemoryCache
import com.squareup.picasso.Picasso
import com.squareup.picasso.cache

class PicassoTinyMemoryCache(private val picasso: Picasso) : TinyMemoryCache {

    override fun get(key: String): CacheBitmap? {
        return picasso.cache[key]
            ?.let { PicassoCacheBitmap(key, it) }
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
        picasso.cache.set(key, bitmap)
        return PicassoCacheBitmap(key, bitmap)
    }
}