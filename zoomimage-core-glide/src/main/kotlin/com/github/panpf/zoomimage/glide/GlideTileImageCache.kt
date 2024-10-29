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

package com.github.panpf.zoomimage.glide

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.bumptech.glide.load.engine.GlideEngine
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.newEngineKey
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.TileImageCache

/**
 * Implement [TileImageCache] based on Glide
 *
 * @see com.github.panpf.zoomimage.core.glide.test.GlideTileImageCacheTest
 */
class GlideTileImageCache(private val glide: Glide) : TileImageCache {

    private val glideEngine: GlideEngine? by lazy {
        createGlideEngine(glide)
    }

    @Suppress("INACCESSIBLE_TYPE")
    override fun get(key: String): TileImage? {
        val engineKey = newEngineKey(key)
        val resource =
            glideEngine?.loadFromMemory(key = engineKey, isMemoryCacheable = true) ?: return null
        return GlideBitmapTileImage(resource = resource, key = key, fromCache = true)
    }

    @Suppress("INACCESSIBLE_TYPE")
    override fun put(
        key: String,
        tileImage: TileImage,
        imageUrl: String,
        imageInfo: ImageInfo,
    ): TileImage? {
        val glideEngine = glideEngine ?: return null
        tileImage as BitmapTileImage
        val bitmap = tileImage.bitmap
        val engineKey = newEngineKey(key)
        val resource = glideEngine.put(bitmap, engineKey)
        return GlideBitmapTileImage(
            resource = EngineResourceWrapper(resource),
            key = tileImage.key,
            fromCache = tileImage.fromCache
        )
    }
}