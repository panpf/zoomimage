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
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.TopStart
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.limitTo
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.calculateContentRotateOrigin
import com.github.panpf.zoomimage.zoom.calculateRotatedContentMoveToTopLeftOffset

/**
 * Calculate basic transformations such as contentScale, alignment, rotation, etc.
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.internal.BaseTransformHelperTest
 */
internal class BaseTransformHelper(
    val containerSize: IntSizeCompat,
    val contentSize: IntSizeCompat,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val rotation: Int,
    val ltrLayout: Boolean = true,
) {
    /*
     * Calculations are based on the following rules:
     * 1. Content is located in the top left corner of the container
     * 2. The scale center point is top left
     * 3. The rotate center point is the content center
     * 4. Apply rotation before scaling and offset
     */

    val rotatedContentSize: IntSizeCompat by lazy { contentSize.rotate(rotation) }

    val scaleFactor: ScaleFactorCompat by lazy {
        contentScale.computeScaleFactor(
            srcSize = rotatedContentSize.toSize(),
            dstSize = containerSize.toSize()
        )
    }

    val scaledRotatedContentSize: SizeCompat by lazy {
        rotatedContentSize.toSize().times(scaleFactor)
    }

    val rotateRectifyOffset: OffsetCompat by lazy {
        val rotatedContentMoveToTopLeftOffset = calculateRotatedContentMoveToTopLeftOffset(
            containerSize = containerSize,
            contentSize = contentSize,
            rotation = rotation,
        )
        rotatedContentMoveToTopLeftOffset * scaleFactor
    }

    val alignmentOffset: OffsetCompat by lazy {
        alignment.align(
            size = scaledRotatedContentSize.round(),
            space = containerSize,
            ltrLayout = ltrLayout
        ).toOffset()
    }

    val offset: OffsetCompat by lazy { rotateRectifyOffset + alignmentOffset }

    val rotationOrigin by lazy {
        calculateContentRotateOrigin(
            containerSize = containerSize,
            contentSize = contentSize
        )
    }

    val displayRect: RectCompat by lazy {
        RectCompat(
            left = alignmentOffset.x,
            top = alignmentOffset.y,
            right = alignmentOffset.x + scaledRotatedContentSize.width,
            bottom = alignmentOffset.y + scaledRotatedContentSize.height,
        )
    }

    val insideDisplayRect: RectCompat by lazy {
        displayRect.limitTo(containerSize.toSize())
    }

    val transform: TransformCompat by lazy {
        TransformCompat(
            scale = scaleFactor,
            scaleOrigin = TransformOriginCompat.TopStart,
            offset = offset,
            rotation = rotation.toFloat(),
            rotationOrigin = rotationOrigin,
        )
    }
}