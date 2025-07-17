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

package com.github.panpf.zoomimage.subsampling

/**
 * A [TileImage] implementation based Bitmap
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.BitmapTileImageTest
 */
@Suppress("RedundantConstructorKeyword")
open class BitmapTileImage constructor(val bitmap: TileBitmap) : TileImage {

    override val width: Int = bitmap.width

    override val height: Int = bitmap.height

    override val byteCount: Long = bitmap.byteCount

    override val isRecycled: Boolean
        get() = bitmap.isRecycled

    override fun recycle() = bitmap.recycle()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BitmapTileImage
        return bitmap == other.bitmap
    }

    override fun hashCode(): Int {
        return bitmap.hashCode()
    }

    override fun toString(): String {
        return "BitmapTileImage(bitmap=${bitmap.toLogString()})"
    }
}