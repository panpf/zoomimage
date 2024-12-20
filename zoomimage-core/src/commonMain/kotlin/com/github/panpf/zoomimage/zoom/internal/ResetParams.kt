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

package com.github.panpf.zoomimage.zoom.internal

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.isNotEmpty
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.isEmpty

// TODO test
data class ResetParams(
    val containerSize: IntSizeCompat,
    val contentSize: IntSizeCompat,
    val contentOriginSize: IntSizeCompat,
    val rotation: Int,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val readMode: ReadMode?,
    val scalesCalculator: ScalesCalculator,
    val limitOffsetWithinBaseVisibleRect: Boolean,
    val containerWhitespaceMultiple: Float,
    val containerWhitespace: ContainerWhitespace,
) {

    val realContainerWhitespace: ContainerWhitespace by lazy {
        val containerWhitespace = containerWhitespace
        val containerSize = containerSize
        val containerWhitespaceMultiple = containerWhitespaceMultiple
        if (!containerWhitespace.isEmpty()) {
            containerWhitespace
        } else if (containerSize.isNotEmpty() && containerWhitespaceMultiple != 0f) {
            ContainerWhitespace(
                horizontal = containerSize.width * containerWhitespaceMultiple,
                vertical = containerSize.height * containerWhitespaceMultiple
            )
        } else {
            ContainerWhitespace.Zero
        }
    }
}

/**
 * Check whether the parameters have changed
 *
 * @return 0: All unchanged; 1: Only containerSize changes; -1: More changes
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ZoomsTest5.testCheckParamsChanges
 */
fun ResetParams.different(other: ResetParams?): Int {
    if (null == other) {
        return -1
    }
    if (this == other) {
        return 0
    }

    if (other.containerSize.isNotEmpty()
        && other.contentSize.isNotEmpty()
        && containerSize.isNotEmpty()
        && contentSize.isNotEmpty()
        && other.containerSize != containerSize
    ) {
        val resetContainerSizeSelf = copy(containerSize = IntSizeCompat.Zero)
        val resetContainerSizeOther = other.copy(containerSize = IntSizeCompat.Zero)
        if (resetContainerSizeSelf == resetContainerSizeOther) {
            return 1
        }
    }

    return -1
}

fun ResetParams.differentProperties(other: ResetParams): String? {
    val differentProperties = mutableListOf<String>()
    if (containerSize != other.containerSize) {
        differentProperties.add("containerSize")
    }
    if (contentSize != other.contentSize) {
        differentProperties.add("contentSize")
    }
    if (contentOriginSize != other.contentOriginSize) {
        differentProperties.add("contentOriginSize")
    }
    if (contentScale != other.contentScale) {
        differentProperties.add("contentScale")
    }
    if (rotation != other.rotation) {
        differentProperties.add("rotation")
    }
    if (alignment != other.alignment) {
        differentProperties.add("alignment")
    }
    if (readMode != other.readMode) {
        differentProperties.add("readMode")
    }
    if (scalesCalculator != other.scalesCalculator) {
        differentProperties.add("scalesCalculator")
    }
    if (limitOffsetWithinBaseVisibleRect != other.limitOffsetWithinBaseVisibleRect) {
        differentProperties.add("limitOffsetWithinBaseVisibleRect")
    }
    if (containerWhitespaceMultiple != other.containerWhitespaceMultiple) {
        differentProperties.add("containerWhitespaceMultiple")
    }
    if (containerWhitespace != other.containerWhitespace) {
        differentProperties.add("containerWhitespace")
    }
    return if (differentProperties.isNotEmpty()) {
        differentProperties.joinToString()
    } else {
        null
    }
}