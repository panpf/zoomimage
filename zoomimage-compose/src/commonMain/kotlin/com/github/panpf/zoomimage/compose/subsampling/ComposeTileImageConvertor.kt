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

import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.TileImage
import com.github.panpf.zoomimage.subsampling.internal.TileImageConvertor

/**
 * Convert [BitmapTileImage] to [ComposeTileImage]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.subsampling.ComposeTileImageConvertorTest
 */
class ComposeTileImageConvertor : TileImageConvertor {

    override suspend fun convert(tileImage: TileImage): TileImage {
        val bitmapTileImage = tileImage as BitmapTileImage
        return ComposeTileImage(bitmapTileImage)
    }
}