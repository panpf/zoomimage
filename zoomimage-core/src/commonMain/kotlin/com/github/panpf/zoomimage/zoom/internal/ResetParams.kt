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
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator

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
)

/**
 * Check whether the parameters have changed
 *
 * @return 0: All unchanged; 1: Only containerSize changes; -1: More changes
 */
fun ResetParams.diff(other: ResetParams?): ResetParamsDiffResult {
    var result = 0
    if (other == null || containerSize != other.containerSize) {
        result = result or ResetParamsDiffResult.FLAG_CONTAINER_SIZE
    }
    if (other == null || contentSize != other.contentSize) {
        result = result or ResetParamsDiffResult.FLAG_CONTENT_SIZE
    }
    if (other == null || contentOriginSize != other.contentOriginSize) {
        result = result or ResetParamsDiffResult.FLAG_CONTENT_ORIGIN_SIZE
    }
    if (other == null || rotation != other.rotation) {
        result = result or ResetParamsDiffResult.FLAG_ROTATION
    }
    if (other == null || contentScale != other.contentScale) {
        result = result or ResetParamsDiffResult.FLAG_CONTENT_SCALE
    }
    if (other == null || alignment != other.alignment) {
        result = result or ResetParamsDiffResult.FLAG_ALIGNMENT
    }
    if (other == null || readMode != other.readMode) {
        result = result or ResetParamsDiffResult.FLAG_READ_MODE
    }
    if (other == null || scalesCalculator != other.scalesCalculator) {
        result = result or ResetParamsDiffResult.FLAG_SCALES_CALCULATOR
    }
    if (other == null || limitOffsetWithinBaseVisibleRect != other.limitOffsetWithinBaseVisibleRect) {
        result = result or ResetParamsDiffResult.FLAG_LIMIT_OFFSET_WITHIN_BASE_VISIBLE_RECT
    }
    if (other == null || containerWhitespaceMultiple != other.containerWhitespaceMultiple) {
        result = result or ResetParamsDiffResult.FLAG_CONTAINER_WHITESPACE_MULTIPLE
    }
    if (other == null || containerWhitespace != other.containerWhitespace) {
        result = result or ResetParamsDiffResult.FLAG_CONTAINER_WHITESPACE
    }
    return ResetParamsDiffResult(result)
}

class ResetParamsDiffResult(val result: Int) {

    val isContainerSizeChanged: Boolean = result and FLAG_CONTAINER_SIZE != 0
    val isContentSizeChanged: Boolean = result and FLAG_CONTENT_SIZE != 0
    val isContentOriginSizeChanged: Boolean = result and FLAG_CONTENT_ORIGIN_SIZE != 0
    val isRotationChanged: Boolean = result and FLAG_ROTATION != 0
    val isContentScaleChanged: Boolean = result and FLAG_CONTENT_SCALE != 0
    val isAlignmentChanged: Boolean = result and FLAG_ALIGNMENT != 0
    val isReadModeChanged: Boolean = result and FLAG_READ_MODE != 0
    val isScalesCalculatorChanged: Boolean = result and FLAG_SCALES_CALCULATOR != 0
    val isLimitOffsetWithinBaseVisibleRectChanged: Boolean =
        result and FLAG_LIMIT_OFFSET_WITHIN_BASE_VISIBLE_RECT != 0
    val isContainerWhitespaceMultipleChanged: Boolean =
        result and FLAG_CONTAINER_WHITESPACE_MULTIPLE != 0
    val isContainerWhitespaceChanged: Boolean = result and FLAG_CONTAINER_WHITESPACE != 0

    private val changeCount: Int = run {
        var count = 0
        if (isContainerSizeChanged) count++
        if (isContentSizeChanged) count++
        if (isContentOriginSizeChanged) count++
        if (isRotationChanged) count++
        if (isContentScaleChanged) count++
        if (isAlignmentChanged) count++
        if (isReadModeChanged) count++
        if (isScalesCalculatorChanged) count++
        if (isLimitOffsetWithinBaseVisibleRectChanged) count++
        if (isContainerWhitespaceMultipleChanged) count++
        if (isContainerWhitespaceChanged) count++
        count
    }

    val isNotChanged: Boolean = changeCount == 0

    val isOnlyContainerSizeChanged: Boolean = changeCount == 1 && isContainerSizeChanged
    val isOnlyContentSizeChanged: Boolean = changeCount == 1 && isContentSizeChanged
    val isOnlyContentOriginSizeChanged: Boolean = changeCount == 1 && isContentOriginSizeChanged
    val isOnlyContentSizeOrContentOriginSizeChanged: Boolean =
        isOnlyContentSizeChanged || isOnlyContentOriginSizeChanged || (changeCount == 2 && isContentSizeChanged && isContentOriginSizeChanged)

    private val changePropertyNames: String by lazy {
        val properties = mutableListOf<String>()
        if (isContainerSizeChanged) properties.add("containerSize")
        if (isContentSizeChanged) properties.add("contentSize")
        if (isContentOriginSizeChanged) properties.add("contentOriginSize")
        if (isRotationChanged) properties.add("rotation")
        if (isContentScaleChanged) properties.add("contentScale")
        if (isAlignmentChanged) properties.add("alignment")
        if (isReadModeChanged) properties.add("readMode")
        if (isScalesCalculatorChanged) properties.add("scalesCalculator")
        if (isLimitOffsetWithinBaseVisibleRectChanged) properties.add("limitOffsetWithinBaseVisibleRect")
        if (isContainerWhitespaceMultipleChanged) properties.add("containerWhitespaceMultiple")
        if (isContainerWhitespaceChanged) properties.add("containerWhitespace")
        properties.toList().joinToString()
    }

    companion object {
        const val FLAG_CONTAINER_SIZE = 0x01
        const val FLAG_CONTENT_SIZE = 0x02
        const val FLAG_CONTENT_ORIGIN_SIZE = 0x04
        const val FLAG_ROTATION = 0x08
        const val FLAG_CONTENT_SCALE = 0x10
        const val FLAG_ALIGNMENT = 0x20
        const val FLAG_READ_MODE = 0x40
        const val FLAG_SCALES_CALCULATOR = 0x80
        const val FLAG_LIMIT_OFFSET_WITHIN_BASE_VISIBLE_RECT = 0x100
        const val FLAG_CONTAINER_WHITESPACE_MULTIPLE = 0x200
        const val FLAG_CONTAINER_WHITESPACE = 0x400
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ResetParamsDiffResult
        return result == other.result
    }

    override fun hashCode(): Int {
        return result
    }

    override fun toString(): String {
        return "ResetParamsDiffResult(${changePropertyNames})"
    }
}