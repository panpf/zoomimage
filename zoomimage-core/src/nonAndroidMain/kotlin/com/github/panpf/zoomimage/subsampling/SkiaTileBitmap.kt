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

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.SkiaBitmap
import com.github.panpf.zoomimage.toLogString

class SkiaTileBitmap(
    val bitmap: SkiaBitmap,
    override val key: String,
    override val bitmapFrom: BitmapFrom,
) : TileBitmap {

    override val width: Int = bitmap.width

    override val height: Int = bitmap.height

    override val byteCount: Long = ((bitmap.rowBytes * bitmap.height).toLong())

    override val isRecycled: Boolean = false

    override fun setIsDisplayed(displayed: Boolean) {

    }

    override fun recycle() {}

    override fun toString(): String {
        return "SkiaTileBitmap(key='$key', bitmap=${bitmap.toLogString()}, bitmapFrom=$bitmapFrom)"
    }
}