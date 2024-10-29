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

/**
 * Tile bitmap interface, you need to implement it to customize tile bitmap
 */
interface TileImage {

    /**
     * The unique identifier of Tile, usually composed of url+srcRect
     */
    val key: String

    /**
     * Tile width
     */
    val width: Int

    /**
     * Tile height
     */
    val height: Int

    /**
     * The number of bytes that can be used to store this tile's pixels
     */
    val byteCount: Long

    /**
     * Whether the tile has been recycled
     */
    val isRecycled: Boolean

    /**
     * Whether it comes from cache
     */
    val fromCache: Boolean

    /**
     * Set whether to display
     */
    fun setIsDisplayed(displayed: Boolean) {

    }

    /**
     * Recycle tile
     */
    fun recycle()
}