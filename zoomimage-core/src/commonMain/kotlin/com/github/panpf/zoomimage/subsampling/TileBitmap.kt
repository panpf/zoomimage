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

package com.github.panpf.zoomimage.subsampling

/**
 * Tile bitmap interface, you need to implement it to customize tile bitmap
 */
interface TileBitmap {
    val width: Int
    val height: Int
    val byteCount: Int
    fun recycle()
    val isRecycled: Boolean
}

/**
 * Tile bitmap interface, you need to implement it to customize tile bitmap
 */
interface CacheTileBitmap : TileBitmap {

    /**
     * Cache key
     */
    val key: String

    /**
     * TileBitmap displays callbacks when the state changes
     */
    fun setIsDisplayed(displayed: Boolean)
}