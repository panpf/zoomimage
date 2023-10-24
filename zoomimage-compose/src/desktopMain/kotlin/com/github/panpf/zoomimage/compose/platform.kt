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

package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.subsampling.ComposeTileBitmap
import com.github.panpf.zoomimage.compose.subsampling.DesktopToComposeTileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.StoppedController
import com.github.panpf.zoomimage.subsampling.TileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.TileSnapshot

@Composable
actual fun defaultStoppedController(): StoppedController? = null

actual fun createTileBitmapConvertor(): TileBitmapConvertor? = DesktopToComposeTileBitmapConvertor()

// todo Let all TileBitmaps on compose implement the ComposeTileBitmap interface, so that there is no need for a separate drawTile
actual fun drawTile(
    contentDrawScope: ContentDrawScope,
    tileSnapshot: TileSnapshot,
    srcOffset: IntOffset,
    srcSize: IntSize,
    dstOffset: IntOffset,
    dstSize: IntSize,
    alpha: Float,
): Boolean {
    val bitmap = (tileSnapshot.bitmap as ComposeTileBitmap?)?.imageBitmap ?: return false
    contentDrawScope.drawImage(
        image = bitmap,
        srcOffset = srcOffset,
        srcSize = srcSize,
        dstOffset = dstOffset,
        dstSize = dstSize,
        alpha = alpha,
    )
    return true
}