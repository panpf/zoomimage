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

package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.format

/**
 * The amount of white space allowed around the container when the image is moved within the container
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContainerWhitespaceTest
 */
data class ContainerWhitespace(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {

    constructor(horizontal: Float, vertical: Float) : this(
        horizontal,
        vertical,
        horizontal,
        vertical
    )

    constructor(size: Float) : this(size, size, size, size)

    companion object {
        val Zero = ContainerWhitespace(size = 0f)
    }
}

/**
 * Check if the white space is valid. The size of the white space is not allowed to be negative.
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContainerWhitespaceTest.testCheck
 */
fun ContainerWhitespace.check(): Boolean {
    return left >= 0f && top >= 0f && right >= 0f && bottom >= 0f
}

/**
 * Check if the white space is empty
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContainerWhitespaceTest.testIsEmpty
 */
fun ContainerWhitespace.isEmpty(): Boolean {
    return left <= 0f && top <= 0f && right <= 0f && bottom <= 0f
}

/**
 * Check if the white space is empty
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContainerWhitespaceTest.testIsEmpty
 */
fun ContainerWhitespace.toShortString(): String {
    return "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"
}

/**
 * Flip the white space horizontally
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContainerWhitespaceTest.testRtlFlipped
 */
fun ContainerWhitespace.rtlFlipped(): ContainerWhitespace {
    return ContainerWhitespace(left = right, top = top, right = left, bottom = bottom)
}