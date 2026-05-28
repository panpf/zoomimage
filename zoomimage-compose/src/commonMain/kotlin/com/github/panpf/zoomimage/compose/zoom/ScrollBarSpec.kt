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
    val color: Color = DEFAULT_COLOR,

    /**
     * Scroll bar size
     */
    val size: Dp = DEFAULT_SIZE,

    /**
     * The distance from the side of the scroll bar to the edge of the screen
     */
    val sideMargin: Dp = DEFAULT_SIDE_MARGIN,

    /**
     * The distance from the scroll bar head and tail to the edge of the screen
     */
    val endsMargin: Dp = DEFAULT_ENDS_MARGIN,

    /**
     * Add the system bars insets, the scroll bar will avoid the position of the system bar
     */
    val windowInsets: WindowInsets? = null,
) {

    // For keep binary compatibility
    @Deprecated(
        message = "This constructor is only for binary compatibility, please use the primary constructor instead",
        level = DeprecationLevel.WARNING
    )
    constructor(
        /**
         * Scroll bar color, which defaults to translucent gray
         */
        color: Color = DEFAULT_COLOR,

        /**
         * Scroll bar size, default to 3 dp
         */
        size: Dp = DEFAULT_SIZE,

        /**
         * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
         */
        margin: Dp = DEFAULT_MARGIN,
    ) : this(
        color = color,
        size = size,
        sideMargin = margin,
        endsMargin = margin * 2,
        windowInsets = null
    )

    companion object {
        val DEFAULT_COLOR = Color(0xB2888888.toInt())
        val DEFAULT_SIZE = 3f.dp
        val DEFAULT_MARGIN = 6f.dp
        val DEFAULT_SIDE_MARGIN = 6f.dp
        val DEFAULT_ENDS_MARGIN = 12f.dp

        val Default = ScrollBarSpec(
            color = DEFAULT_COLOR,
            size = DEFAULT_SIZE,
            sideMargin = DEFAULT_SIDE_MARGIN,
            endsMargin = DEFAULT_ENDS_MARGIN,
            windowInsets = null,
        )

        val Medium = ScrollBarSpec(
            color = DEFAULT_COLOR,
            size = 5.dp,
            sideMargin = 10.dp,
            endsMargin = 20.dp,
            windowInsets = null,
        )

        val Large = ScrollBarSpec(
            color = DEFAULT_COLOR,
            size = 7.dp,
            sideMargin = 14.dp,
            endsMargin = 28.dp,
            windowInsets = null,
        )
    }
}

fun ScrollBarSpec.toWindowInsets(): WindowInsets = WindowInsets(
    left = 0.dp,
    top = 0.dp,
    right = ((sideMargin * 2) + size),
    bottom = ((sideMargin * 2) + size),
)