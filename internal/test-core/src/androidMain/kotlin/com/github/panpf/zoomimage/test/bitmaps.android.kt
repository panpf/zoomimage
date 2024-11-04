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

package com.github.panpf.zoomimage.test

import android.graphics.Bitmap
import android.media.ThumbnailUtils

actual fun createBitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
}

actual fun createA8Bitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
}

actual fun createRGB565Bitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
}

fun createBitmap(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap = Bitmap.createBitmap(width, height, config)

fun Bitmap.copyWith(
    config: Bitmap.Config = this.config,
    isMutable: Boolean = isMutable()
): Bitmap {
    return this.copy(/* config = */ config, /* isMutable = */ isMutable)
}

/**
 * Create thumbnails with specified width and height
 */
actual fun Bitmap.thumbnail(width: Int, height: Int): Bitmap {
    val outputBitmap = ThumbnailUtils.extractThumbnail(this, width, height)
    return outputBitmap
}

/**
 * Returns the Color at the specified location. Format ARGB_8888
 */
actual fun Bitmap.readIntPixel(x: Int, y: Int): Int = getPixel(x, y)