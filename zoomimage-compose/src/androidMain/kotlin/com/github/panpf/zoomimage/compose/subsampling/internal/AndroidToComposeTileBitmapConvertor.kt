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

import com.github.panpf.zoomimage.compose.subsampling.AndroidCacheComposeTileBitmap
import com.github.panpf.zoomimage.compose.subsampling.AndroidComposeTileBitmap
import com.github.panpf.zoomimage.subsampling.AndroidCacheTileBitmap
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor

class AndroidToComposeTileBitmapConvertor : TileBitmapConvertor {

    override suspend fun convert(tileBitmap: TileBitmap): TileBitmap {
        val androidTileBitmap = tileBitmap as AndroidTileBitmap
        return if (androidTileBitmap is AndroidCacheTileBitmap) {
            AndroidCacheComposeTileBitmap(androidTileBitmap)
        } else {
            AndroidComposeTileBitmap(androidTileBitmap)
        }
    }
}