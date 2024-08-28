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

import com.github.panpf.zoomimage.annotation.IntDef

/**
 * Tile state
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileStateTest
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    TileState.STATE_NONE,
    TileState.STATE_LOADING,
    TileState.STATE_LOADED,
    TileState.STATE_ERROR
)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class TileState {

    companion object {

        const val STATE_NONE = 0
        const val STATE_LOADING = 1
        const val STATE_LOADED = 2
        const val STATE_ERROR = 3

        fun name(state: Int): String = when (state) {
            STATE_NONE -> "NONE"
            STATE_LOADING -> "LOADING"
            STATE_LOADED -> "LOADED"
            STATE_ERROR -> "ERROR"
            else -> "UNKNOWN"
        }
    }
}

const val TILE_COLOR_RED: Int = 0xFFFF0000.toInt()
const val TILE_COLOR_GREEN: Int = 0xFF00FF00.toInt()
const val TILE_COLOR_YELLOW_GREEN: Int = 0xFF9ACD32.toInt()
const val TILE_COLOR_BLUE: Int = 0xFF0000FF.toInt()
const val TILE_COLOR_YELLOW: Int = 0xFFFFFF00.toInt()
const val TILE_COLOR_CYAN: Int = 0xFF00FFFF.toInt()
const val TILE_COLOR_MAGENTA: Int = 0xFFFF00FF.toInt()
const val TILE_COLOR_SKY_BLUE: Int = 0xFF00CCFF.toInt()

/**
 * Get the color of the tile according to the state
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileStateTest.testTileStateColor
 */
fun tileStateColor(
    @TileState state: Int,
    withinLoadArea: Boolean,
    bitmapFrom: BitmapFrom,
): Int = when {
    !withinLoadArea -> TILE_COLOR_SKY_BLUE
    state == TileState.STATE_LOADED -> {
        when (bitmapFrom) {
            BitmapFrom.MEMORY_CACHE -> TILE_COLOR_GREEN
            BitmapFrom.LOCAL -> TILE_COLOR_YELLOW_GREEN
        }
    }

    state == TileState.STATE_LOADING -> TILE_COLOR_CYAN
    else -> TILE_COLOR_RED
}