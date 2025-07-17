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

const val TILE_COLOR_RED: Int = 0xFFFF0000.toInt()
const val TILE_COLOR_GREEN: Int = 0xFF00FF00.toInt()
const val TILE_COLOR_YELLOW: Int = 0xFFFFFF00.toInt()
const val TILE_COLOR_SKY_BLUE: Int = 0xFF00CCFF.toInt()
const val TILE_COLOR_LIGHT_GRAY: Int = 0xFFCCCCCC.toInt()

/**
 * Get the color of the tile according to the state
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileColorTest.testTileColor
 */
@Deprecated("Use tileColor(state, from, withinLoadArea) instead")
fun tileColor(
    @TileState state: Int,
    withinLoadArea: Boolean,
    fromCache: Boolean,
): Int = when {
    !withinLoadArea -> TILE_COLOR_LIGHT_GRAY
    state == TileState.STATE_LOADED -> if (fromCache) TILE_COLOR_GREEN else TILE_COLOR_SKY_BLUE
    state == TileState.STATE_LOADING -> TILE_COLOR_YELLOW
    else -> TILE_COLOR_RED
}

/**
 * Get the color of the tile according to the state
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileColorTest.testTileColor
 */
fun tileColor(
    @TileState state: Int,
    @TileImageFrom from: Int,
    withinLoadArea: Boolean,
): Int = when {
    !withinLoadArea -> TILE_COLOR_LIGHT_GRAY
    state == TileState.STATE_LOADED -> if (from == TileImageFrom.MEMORY_CACHE) TILE_COLOR_GREEN else TILE_COLOR_SKY_BLUE
    state == TileState.STATE_LOADING -> TILE_COLOR_YELLOW
    else -> TILE_COLOR_RED
}