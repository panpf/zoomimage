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
     * Scroll bar size, default to 3 dp
     */
    val size: Float = DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,  // TODO Split into four attributes: leftMargin, topMargin, rightMargin, bottomMargin

    /**
     * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
     */
    val margin: Float = DEFAULT_MARGIN * Resources.getSystem().displayMetrics.density,

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
        color: Int = DEFAULT_COLOR,

        /**
         * Scroll bar size, default to 3 dp
         */
        size: Float = DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,

        /**
         * The distance of the scroll bar from the edge of the container, which defaults to 6 dp
         */
        margin: Float = DEFAULT_MARGIN * Resources.getSystem().displayMetrics.density,
    ) : this(color = color, size = size, margin = margin, enabledWindowInsets = false)

    companion object {
        const val DEFAULT_COLOR = 0xB2888888.toInt()
        const val DEFAULT_SIZE = 3f
        const val DEFAULT_MARGIN = 6f
        val Default = ScrollBarSpec()
        val DefaultAndWindowInsets = ScrollBarSpec(enabledWindowInsets = true)
        // TODO Add Large presets
    }
}

fun ScrollBarSpec.toInsets(): Insets = Insets.of(
    /* left = */ 0,
    /* top = */ 0,
    /* right = */ ((margin * 2) + size).roundToInt(),  // TODO No more multiplying by 2
    /* bottom = */ ((margin * 2) + size).roundToInt()  // TODO No more multiplying by 2
)