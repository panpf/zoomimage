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
 * Tile animation configuration
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.TileAnimationSpecTest
 */
data class TileAnimationSpec(
    /**
     * Animation duration
     */
    val duration: Long = DEFAULT_DURATION,

    /**
     * Animation refresh interval
     */
    val interval: Long = DEFAULT_INTERVAL
) {

    companion object {
        const val DEFAULT_DURATION = 200L
        const val DEFAULT_INTERVAL = 8L

        val Default = TileAnimationSpec(duration = DEFAULT_DURATION, interval = DEFAULT_INTERVAL)
        val None = TileAnimationSpec(duration = 0L, interval = DEFAULT_INTERVAL)
    }
}