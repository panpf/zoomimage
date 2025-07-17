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
@Suppress("RedundantConstructorKeyword")
internal class GlideBitmapTileImage constructor(
    private val resource: EngineResourceWrapper,
) : BitmapTileImage(resource.bitmap) {

    override fun setIsDisplayed(displayed: Boolean) {
        resource.setIsDisplayed(displayed)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GlideBitmapTileImage
        return resource.bitmap == other.resource.bitmap
    }

    override fun hashCode(): Int {
        return resource.bitmap.hashCode()
    }

    override fun toString(): String {
        return "GlideBitmapTileImage(bitmap=${bitmap.toLogString()})"
    }
}