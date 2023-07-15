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
package com.github.panpf.zoomimage.view.coil.internal

import android.graphics.Bitmap
import coil.memory.MemoryCache
import com.github.panpf.zoomimage.subsampling.TileBitmap

class CoilTileBitmap(
    override val key: String,
    private val cacheValue: MemoryCache.Value
) : TileBitmap {

    override val bitmap: Bitmap
        get() = cacheValue.bitmap

    override fun setIsDisplayed(displayed: Boolean) {

    }
}