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

import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.toLogString

/**
 * [TileImage] implementation based on Glide
 *
 * @see com.github.panpf.zoomimage.core.glide.test.GlideBitmapTileImageTest
 */
internal class GlideBitmapTileImage(
    private val resource: EngineResourceWrapper,
    key: String,
    fromCache: Boolean
) : BitmapTileImage(resource.bitmap, key, fromCache) {

    override fun setIsDisplayed(displayed: Boolean) {
        resource.setIsDisplayed(displayed)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GlideBitmapTileImage
        if (resource.bitmap != other.resource.bitmap) return false
        if (key != other.key) return false
        if (fromCache != other.fromCache) return false
        return true
    }

    override fun hashCode(): Int {
        var result = resource.bitmap.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + fromCache.hashCode()
        return result
    }

    override fun toString(): String {
        return "GlideBitmapTileImage(bitmap=${bitmap.toLogString()}, key='$key', fromCache=$fromCache)"
    }
}