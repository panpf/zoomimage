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

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat

/**
 * Tile-related information provided to users
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileSnapshotTest
 */
data class TileSnapshot(
    val coordinate: IntOffsetCompat,
    val srcRect: IntRectCompat,
    val sampleSize: Int,
    val tileImage: TileImage?,
    @TileState val state: Int,
    val alpha: Int,
)

/**
 * Convert [Tile] to [TileSnapshot]
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileSnapshotTest.testToSnapshot
 */
fun Tile.toSnapshot(): TileSnapshot = TileSnapshot(
    coordinate = coordinate,
    srcRect = srcRect,
    sampleSize = sampleSize,
    tileImage = tileImage,
    state = state,
    alpha = animationState.alpha
)