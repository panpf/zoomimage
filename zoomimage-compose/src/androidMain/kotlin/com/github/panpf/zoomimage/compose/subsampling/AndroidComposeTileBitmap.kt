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

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.github.panpf.zoomimage.subsampling.AndroidCacheTileBitmap
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.CacheTileBitmap

class AndroidComposeTileBitmap constructor(
    private val androidTileBitmap: AndroidTileBitmap,
    override val bitmapFrom: BitmapFrom
) : ComposeTileBitmap {

    override val imageBitmap: ImageBitmap = androidTileBitmap.bitmap!!.asImageBitmap()

    override val width: Int
        get() = androidTileBitmap.width

    override val height: Int
        get() = androidTileBitmap.height

    override val byteCount: Int
        get() = androidTileBitmap.byteCount

    override val isRecycled: Boolean
        get() = androidTileBitmap.isRecycled

    override fun recycle() {
        androidTileBitmap.recycle()
    }

    override fun toString(): String {
        return "AndroidComposeTileBitmap(bitmap=$androidTileBitmap, bitmapFrom=$bitmapFrom)"
    }
}

class AndroidCacheComposeTileBitmap constructor(
    private val androidTileBitmap: AndroidCacheTileBitmap,
    override val bitmapFrom: BitmapFrom
) : ComposeTileBitmap, CacheTileBitmap {

    override val imageBitmap: ImageBitmap = androidTileBitmap.bitmap!!.asImageBitmap()

    override val key: String
        get() = androidTileBitmap.key

    override fun setIsDisplayed(displayed: Boolean) {
        androidTileBitmap.setIsDisplayed(displayed)
    }

    override val width: Int
        get() = androidTileBitmap.width

    override val height: Int
        get() = androidTileBitmap.height

    override val byteCount: Int
        get() = androidTileBitmap.byteCount

    override val isRecycled: Boolean
        get() = androidTileBitmap.isRecycled

    override fun recycle() {
        androidTileBitmap.recycle()
    }

    override fun toString(): String {
        return "AndroidCacheComposeTileBitmap(bitmap=$androidTileBitmap, bitmapFrom=$bitmapFrom)"
    }
}