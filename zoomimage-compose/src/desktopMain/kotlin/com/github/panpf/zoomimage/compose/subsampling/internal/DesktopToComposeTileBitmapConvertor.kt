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

import androidx.compose.ui.graphics.toComposeImageBitmap
import com.github.panpf.zoomimage.compose.subsampling.DesktopComposeTileBitmap
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DesktopToComposeTileBitmapConvertor : TileBitmapConvertor {

    override suspend fun convert(tileBitmap: TileBitmap): TileBitmap {
        val desktopTileBitmap = tileBitmap as DesktopTileBitmap
        val imageBitmap = withContext(Dispatchers.IO) {
            desktopTileBitmap.bufferedImage.toComposeImageBitmap()
        }
        return DesktopComposeTileBitmap(
            imageBitmap = imageBitmap,
            bitmapByteCount = tileBitmap.byteCount
        )
    }
}