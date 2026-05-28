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

package com.github.panpf.zoomimage.view.zoom

import android.content.res.Resources
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

/**
 * Used to configure the style of the scroll bar
 *
 * @see com.github.panpf.zoomimage.view.test.zoom.ScrollBarSpecTest
 */
data class ScrollBarSpec(
    /**
     * Scroll bar color, which defaults to translucent gray
     */
    val color: Int = DEFAULT_COLOR,

    /**
     * Scroll bar size
     */
    val size: Float = DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,

    /**
     * The distance from the side of the scroll bar to the edge of the screen
     */
    val sideMargin: Float = DEFAULT_SIDE_MARGIN * Resources.getSystem().displayMetrics.density,

    /**
     * The distance from the scroll bar head and tail to the edge of the screen
     */
    val endsMargin: Float = DEFAULT_ENDS_MARGIN * Resources.getSystem().displayMetrics.density,

    /**
     * Add the system bars insets, the scroll bar will avoid the position of the system bar
     */
    @property:WindowInsetsCompat.Type.InsetsType
    val windowInsetsTypeMask: Int? = null,
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
        color: Int = DEFAULT_COLOR,

        /**
         * Scroll bar size, default to 3 dp
         */
        size: Float = DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,

        /**
         * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
         */
        margin: Float = DEFAULT_MARGIN * Resources.getSystem().displayMetrics.density,
    ) : this(
        color = color,
        size = size,
        sideMargin = margin,
        endsMargin = margin * 2,
        windowInsetsTypeMask = null,
    )

    companion object {
        const val DEFAULT_COLOR = 0xB2888888.toInt()
        const val DEFAULT_SIZE = 3f
        const val DEFAULT_MARGIN = 6f
        const val DEFAULT_SIDE_MARGIN = 6f
        const val DEFAULT_ENDS_MARGIN = 12f

        val Default = ScrollBarSpec(
            color = DEFAULT_COLOR,
            size = DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,
            sideMargin = DEFAULT_SIDE_MARGIN * Resources.getSystem().displayMetrics.density,
            endsMargin = DEFAULT_ENDS_MARGIN * Resources.getSystem().displayMetrics.density,
            windowInsetsTypeMask = null,
        )

        val Medium = ScrollBarSpec(
            color = DEFAULT_COLOR,
            size = 5 * Resources.getSystem().displayMetrics.density,
            sideMargin = 10 * Resources.getSystem().displayMetrics.density,
            endsMargin = 20 * Resources.getSystem().displayMetrics.density,
            windowInsetsTypeMask = null,
        )

        val Large = ScrollBarSpec(
            color = DEFAULT_COLOR,
            size = 7 * Resources.getSystem().displayMetrics.density,
            sideMargin = 14 * Resources.getSystem().displayMetrics.density,
            endsMargin = 28 * Resources.getSystem().displayMetrics.density,
            windowInsetsTypeMask = null,
        )
    }
}

fun ScrollBarSpec.toInsets(): Insets = Insets.of(
    /* left = */ 0,
    /* top = */ 0,
    /* right = */ ((sideMargin * 2) + size).roundToInt(),
    /* bottom = */ ((sideMargin * 2) + size).roundToInt(),
)

fun ScrollBarSpec.toInsetsOnlyBottom(): Insets = Insets.of(
    /* left = */ 0,
    /* top = */ 0,
    /* right = */ 0,
    /* bottom = */ ((sideMargin * 2) + size).roundToInt(),
)

fun ScrollBarSpec.toInsetsOnlyRight(): Insets = Insets.of(
    /* left = */ 0,
    /* top = */ 0,
    /* right = */ ((sideMargin * 2) + size).roundToInt(),
    /* bottom = */ 0,
)