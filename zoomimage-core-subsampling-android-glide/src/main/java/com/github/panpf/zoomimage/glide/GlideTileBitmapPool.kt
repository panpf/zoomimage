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

package com.github.panpf.zoomimage.glide

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmapPool

class GlideTileBitmapPool(private val glide: Glide) : AndroidTileBitmapPool {

    override fun put(bitmap: Bitmap): Boolean {
        glide.bitmapPool.put(bitmap)
        return true
    }

    override fun get(width: Int, height: Int, config: Config): Bitmap? {
        return glide.bitmapPool.get(width, height, config)
    }
}