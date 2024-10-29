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

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.graphics.ImageBitmap
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.TileImage

/**
 * [TileImage] implementation based on [ImageBitmap]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.subsampling.ComposeTileImageTest
 */
class ComposeTileImage(
    private val tileImage: BitmapTileImage,
) : TileImage by tileImage {

    val bitmap: ImageBitmap = tileImage.bitmap.asComposeBitmap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ComposeTileImage
        return tileImage == other.tileImage
    }

    override fun hashCode(): Int {
        return tileImage.hashCode()
    }

    override fun toString(): String {
        return "ComposeTileImage(tileImage=$tileImage)"
    }
}