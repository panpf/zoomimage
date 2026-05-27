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

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Used to configure the style of the scroll bar
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ScrollBarSpecTest
 */
@Immutable
data class ScrollBarSpec(
    /**
     * Scroll bar color, which defaults to translucent gray
     */
    val color: Color = Color(0xB2888888),

    /**
     * Scroll bar size, default to 3 dp
     */
    val size: Dp = 3.dp,

    /**
     * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
     */
    val margin: Dp = 6.dp,  // TODO Split into four attributes: leftMargin, topMargin, rightMargin, bottomMargin

    /**
     * Whether to enable the scroll bar to avoid being covered by system window insets, which defaults to false
     */
    val enabledWindowInsets: Boolean = false,
) {

    // For keep binary compatibility
    constructor(
        /**
         * Scroll bar color, which defaults to translucent gray
         */
        color: Color = Color(0xB2888888),

        /**
         * Scroll bar size, default to 3 dp
         */
        size: Dp = 3.dp,

        /**
         * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
         */
        margin: Dp = 6.dp,
    ) : this(color = color, size = size, margin = margin, enabledWindowInsets = false)

    companion object {
        val Default = ScrollBarSpec()
        val DefaultAndWindowInsets = ScrollBarSpec(enabledWindowInsets = true)
        // TODO Add Large presets
//        val Large = ScrollBarSpec(size = 6.dp, margin = 12.dp)
//        val LargeAndWindowInsets = ScrollBarSpec(size = 6.dp, margin = 12.dp, enabledWindowInsets = true)
    }
}

fun ScrollBarSpec.toWindowInsets(): WindowInsets = WindowInsets(
    left = 0.dp,
    top = 0.dp,
    right = ((margin * 2) + size),  // TODO No more multiplying by 2
    bottom = ((margin * 2) + size),  // TODO No more multiplying by 2
)