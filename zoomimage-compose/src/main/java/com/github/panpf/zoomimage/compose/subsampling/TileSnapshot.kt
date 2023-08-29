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

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntRect
import com.github.panpf.zoomimage.subsampling.Tile.State

/**
 * A snapshot of the tile
 */
@Immutable
data class TileSnapshot(
    /**
     * The region of Tile in the original image
     */
    val srcRect: IntRect,

    /**
     * The sampling multiplier at load
     */
    val inSampleSize: Int,

    /**
     * The bitmap of the tile
     */
    val bitmap: Bitmap?,

    /**
     * The state of the tile
     */
    @State
    val state: Int,
)