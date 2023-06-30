/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.internal

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.Size
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val matrixValuesLocal = ThreadLocal<FloatArray>()
private val Matrix.localValues: FloatArray
    get() {
        val values = matrixValuesLocal.get()
            ?: FloatArray(9).apply { matrixValuesLocal.set(this) }
        getValues(values)
        return values
    }

internal fun Matrix.getScale(): ScaleFactor {
    val values = localValues
    return ScaleFactor(
        scaleX = values[Matrix.MSCALE_X],
        scaleY = values[Matrix.MSCALE_Y]
    )
}

internal fun Matrix.getTranslation(): Translation {
    val values = localValues
    return Translation(
        translationX = values[Matrix.MTRANS_X],
        translationY = values[Matrix.MTRANS_Y]
    )
}

internal fun Matrix.getRotateDegrees(): Int {
    val values = localValues
    val skewX: Float = values[Matrix.MSKEW_X]
    val scaleX: Float = values[Matrix.MSCALE_X]
    val degrees = (atan2(skewX.toDouble(), scaleX.toDouble()) * (180 / Math.PI)).roundToInt()
    return when {
        degrees < 0 -> abs(degrees)
        degrees > 0 -> 360 - degrees
        else -> 0
    }
}

internal fun reverseRotateRect(rect: Rect, rotateDegrees: Int, drawableSize: Size) {
    require(rotateDegrees % 90 == 0) {
        "rotateDegrees must be an integer multiple of 90"
    }
    when (rotateDegrees) {
        90 -> {
            val bottom = rect.bottom
            rect.bottom = rect.left
            rect.left = rect.top
            rect.top = rect.right
            rect.right = bottom
            rect.top = drawableSize.height - rect.top
            rect.bottom = drawableSize.height - rect.bottom
        }

        180 -> {
            var right = rect.right
            rect.right = rect.left
            rect.left = right
            right = rect.bottom
            rect.bottom = rect.top
            rect.top = right
            rect.top = drawableSize.height - rect.top
            rect.bottom = drawableSize.height - rect.bottom
            rect.left = drawableSize.width - rect.left
            rect.right = drawableSize.width - rect.right
        }

        270 -> {
            val bottom = rect.bottom
            rect.bottom = rect.right
            rect.right = rect.top
            rect.top = rect.left
            rect.left = bottom
            rect.left = drawableSize.width - rect.left
            rect.right = drawableSize.width - rect.right
        }
    }
}

internal fun rotatePoint(point: PointF, rotateDegrees: Int, drawableSize: Size) {
    require(rotateDegrees % 90 == 0) {
        "rotateDegrees must be an integer multiple of 90"
    }
    when (rotateDegrees) {
        90 -> {
            point.x = drawableSize.height - point.y
            point.y = point.x
        }

        180 -> {
            point.x = drawableSize.width - point.x
            point.y = drawableSize.height - point.y
        }

        270 -> {
            point.x = point.y
            point.y = drawableSize.width - point.x
        }
    }
}

internal fun ScaleType.computeTransform(srcSize: Size, dstSize: Size): Transform {
    val scaleFactor = this.computeScaleFactor(srcSize, dstSize)
    val translation = computeScaleTranslation(srcSize, dstSize, this)
    return Transform(scaleFactor, translation)
}

internal fun ScaleType.computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    val fillMinDimension = min(widthScale, heightScale)
    return when (this) {
        ScaleType.CENTER -> ScaleFactor(scaleX = 1.0f, scaleY = 1.0f)

        ScaleType.CENTER_CROP -> {
            ScaleFactor(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
        }

        ScaleType.CENTER_INSIDE -> {
            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
                ScaleFactor(scaleX = 1.0f, scaleY = 1.0f)
            } else {
                ScaleFactor(scaleX = fillMinDimension, scaleY = fillMinDimension)
            }
        }

        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END -> {
            ScaleFactor(scaleX = fillMinDimension, scaleY = fillMinDimension)
        }

        ScaleType.FIT_XY -> {
            ScaleFactor(scaleX = widthScale, scaleY = heightScale)
        }

        ScaleType.MATRIX -> ScaleFactor(1.0f, 1.0f)
        else -> ScaleFactor(scaleX = 1.0f, scaleY = 1.0f)
    }
}

internal fun computeScaleTranslation(
    srcSize: Size,
    dstSize: Size,
    scaleType: ScaleType
): Translation {
    val scaleFactor = scaleType.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    val scaledSrcSize = srcSize.times(scaleFactor)
    return when (scaleType) {
        ScaleType.CENTER -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.CENTER_CROP -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.CENTER_INSIDE -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.FIT_START -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )

        ScaleType.FIT_CENTER -> Translation(
            translationX = (dstSize.width - scaledSrcSize.width) / 2.0f,
            translationY = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.FIT_END -> Translation(
            translationX = dstSize.width - scaledSrcSize.width.toFloat(),
            translationY = dstSize.height - scaledSrcSize.height.toFloat()
        )

        ScaleType.FIT_XY -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )

        ScaleType.MATRIX -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )

        else -> Translation(
            translationX = 0.0f,
            translationY = 0.0f
        )
    }
}

internal fun ScaleType.supportReadMode(): Boolean = this != ScaleType.FIT_XY

internal fun computeReadModeTransform(
    srcSize: Size,
    dstSize: Size,
    scaleType: ScaleType,
): Transform {
    return com.github.panpf.zoomimage.core.internal.computeReadModeTransform(
        srcSize = srcSize,
        dstSize = dstSize,
        baseTransform = scaleType.computeTransform(srcSize = srcSize, dstSize = dstSize)
    )
}


fun ScaleType.toScaleMode(): ScaleMode = when (this) {
    ScaleType.CENTER -> ScaleMode.NONE
    ScaleType.CENTER_CROP -> ScaleMode.CROP
    ScaleType.CENTER_INSIDE -> ScaleMode.INSIDE
    ScaleType.FIT_START -> ScaleMode.FIT
    ScaleType.FIT_CENTER -> ScaleMode.FIT
    ScaleType.FIT_END -> ScaleMode.FIT
    ScaleType.FIT_XY -> ScaleMode.FILL_BOUNDS
    ScaleType.MATRIX -> ScaleMode.NONE
    else -> ScaleMode.NONE
}

internal fun computeContentInContainerRect(
    containerSize: Size,
    contentSize: Size,
    scaleType: ScaleType,
): Rect {
    if (containerSize.isEmpty || contentSize.isEmpty) return ZeroRect
    val contentScaleFactor =
        scaleType.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
    val contentScaledContentSize = contentSize.times(contentScaleFactor)
    val translation = computeScaleTranslation(
        srcSize = contentSize,
        dstSize = containerSize,
        scaleType = scaleType,
    )
    return Rect(
        left = translation.translationX.coerceAtLeast(0f).roundToInt(),
        top = translation.translationY.coerceAtLeast(0f).roundToInt(),
        right = (translation.translationX + contentScaledContentSize.width).roundToInt()
            .coerceAtMost(containerSize.width),
        bottom = (translation.translationY + contentScaledContentSize.height).roundToInt()
            .coerceAtMost(containerSize.height),
    )
}


internal fun computeSupportTranslationBounds(
    containerSize: Size,
    contentSize: Size,
    scaleType: ScaleType,
    supportScale: Float
): Rect {
    // based on the top left zoom
    if (supportScale <= 1.0f || containerSize.isEmpty || contentSize.isEmpty) {
        return ZeroRect
    }
    val scaledContainerSize = containerSize.times(supportScale)
    val scaledContentInContainerRect = computeContentInContainerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        scaleType = scaleType,
    ).scale(supportScale)

    val horizontalBounds = if (scaledContentInContainerRect.width() > containerSize.width) {
        ((scaledContentInContainerRect.right - containerSize.width) * -1)..(scaledContentInContainerRect.left * -1)
    } else if (scaleType.isStart(srcSize = contentSize, dstSize = containerSize)) {
        0..0
    } else if (scaleType.isHorizontalCenter(srcSize = contentSize, dstSize = containerSize)) {
        val horizontalSpace = (scaledContainerSize.width - containerSize.width) / 2 * -1
        horizontalSpace..horizontalSpace
    } else {   // contentAlignment.isEnd
        val horizontalSpace = (scaledContainerSize.width - containerSize.width) * -1
        horizontalSpace..horizontalSpace
    }

    val verticalBounds = if (scaledContentInContainerRect.height() > containerSize.height) {
        ((scaledContentInContainerRect.bottom - containerSize.height) * -1)..(scaledContentInContainerRect.top * -1)
    } else if (scaleType.isTop(srcSize = contentSize, dstSize = containerSize)) {
        0..0
    } else if (scaleType.isVerticalCenter(srcSize = contentSize, dstSize = containerSize)) {
        val verticalSpace = (scaledContainerSize.height - containerSize.height) / 2 * -1
        verticalSpace..verticalSpace
    } else {   // contentAlignment.isBottom
        val verticalSpace = (scaledContainerSize.height - containerSize.height) * -1
        verticalSpace..verticalSpace
    }

    return Rect(
        left = horizontalBounds.first,
        top = verticalBounds.first,
        right = horizontalBounds.last,
        bottom = verticalBounds.last
    )
}


internal fun ScaleType.isStart(srcSize: Size, dstSize: Size): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.width < dstSize.width)
}

internal fun ScaleType.isHorizontalCenter(srcSize: Size, dstSize: Size): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.width >= dstSize.width)
            || (this == ScaleType.FIT_END && scaledSrcSize.width >= dstSize.width)
}

internal fun ScaleType.isCenter(srcSize: Size, dstSize: Size): Boolean =
    this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER

internal fun ScaleType.isEnd(srcSize: Size, dstSize: Size): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.width < dstSize.width
}

internal fun ScaleType.isTop(srcSize: Size, dstSize: Size): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.height < dstSize.height)
}

internal fun ScaleType.isVerticalCenter(srcSize: Size, dstSize: Size): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.height >= dstSize.height)
            || (this == ScaleType.FIT_END && scaledSrcSize.height >= dstSize.height)
}

internal fun ScaleType.isBottom(srcSize: Size, dstSize: Size): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.height < dstSize.height
}
