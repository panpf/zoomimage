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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.bumptech.glide.load.engine.GlideEngine
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.newEngineKey
import com.github.panpf.zoomimage.core.CacheBitmap
import com.github.panpf.zoomimage.core.Size
import com.github.panpf.zoomimage.core.TinyMemoryCache

class GlideTinyMemoryCache(private val glide: Glide) : TinyMemoryCache {

    private val glideEngine: GlideEngine? by lazy {
        createGlideEngine(glide)
    }

    @Suppress("INACCESSIBLE_TYPE")
    override fun get(key: String): CacheBitmap? {
        val engineKey = newEngineKey(key)
        val resource = glideEngine?.loadFromMemory(key = engineKey, isMemoryCacheable = true)
        return resource?.let { GlideCacheBitmap(key, it) }
    }

    @Suppress("INACCESSIBLE_TYPE")
    override fun put(
        key: String,
        bitmap: Bitmap,
        imageKey: String,
        imageSize: Size,
        imageMimeType: String,
        imageExifOrientation: Int,
        disallowReuseBitmap: Boolean
    ): CacheBitmap? {
        val engineKey = newEngineKey(key)
        val resource = glideEngine?.put(bitmap, engineKey) ?: return null
        return GlideCacheBitmap(key, EngineResourceWrapper(resource))
    }
}