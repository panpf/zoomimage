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

package com.github.panpf.zoomimage.compose.subsampling.internal

import androidx.compose.ui.graphics.asComposeImageBitmap
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor

class SkiaToComposeTileBitmapConvertor : TileBitmapConvertor {

    override suspend fun convert(tileBitmap: TileBitmap): TileBitmap {
        val desktopTileBitmap = tileBitmap as SkiaTileBitmap
        val imageBitmap = desktopTileBitmap.bitmap.asComposeImageBitmap()
        return SkiaComposeTileBitmap(
            imageBitmap = imageBitmap,
            bitmapByteCount = tileBitmap.byteCount,
            bitmapFrom = tileBitmap.bitmapFrom
        )
    }
}