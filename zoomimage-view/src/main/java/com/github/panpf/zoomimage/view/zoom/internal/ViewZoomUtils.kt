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
package com.github.panpf.zoomimage.view.zoom.internal

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.TransformCompat
import com.github.panpf.zoomimage.core.internal.ScaleMode
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.core.times
import com.github.panpf.zoomimage.view.internal.Rect
import com.github.panpf.zoomimage.view.internal.ZeroRect
import com.github.panpf.zoomimage.view.internal.scale
import com.github.panpf.zoomimage.view.internal.times
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

internal fun Matrix.getScale(): ScaleFactorCompat {
    val values = localValues
    return ScaleFactorCompat(
        scaleX = values[Matrix.MSCALE_X],
        scaleY = values[Matrix.MSCALE_Y]
    )
}

internal fun Matrix.getTranslation(): OffsetCompat {
    val values = localValues
    return OffsetCompat(
        x = values[Matrix.MTRANS_X],
        y = values[Matrix.MTRANS_Y]
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

internal fun reverseRotateRect(rect: Rect, rotateDegrees: Int, drawableSize: IntSizeCompat) {
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

internal fun rotatePoint(point: PointF, rotateDegrees: Int, drawableSize: IntSizeCompat) {
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

internal fun ScaleType.computeTransform(srcSize: IntSizeCompat, dstSize: IntSizeCompat): TransformCompat {
    val scaleFactor = this.computeScaleFactor(srcSize, dstSize)
    val offset = computeContentScaleOffset(srcSize, dstSize, this)
    return TransformCompat(scale = scaleFactor, offset = offset)
}

internal fun ScaleType.computeScaleFactor(srcSize: IntSizeCompat, dstSize: IntSizeCompat): ScaleFactorCompat {
    val widthScale = dstSize.width / srcSize.width.toFloat()
    val heightScale = dstSize.height / srcSize.height.toFloat()
    val fillMaxDimension = max(widthScale, heightScale)
    val fillMinDimension = min(widthScale, heightScale)
    return when (this) {
        ScaleType.CENTER -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)

        ScaleType.CENTER_CROP -> {
            ScaleFactorCompat(scaleX = fillMaxDimension, scaleY = fillMaxDimension)
        }

        ScaleType.CENTER_INSIDE -> {
            if (srcSize.width <= dstSize.width && srcSize.height <= dstSize.height) {
                ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
            } else {
                ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
            }
        }

        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END -> {
            ScaleFactorCompat(scaleX = fillMinDimension, scaleY = fillMinDimension)
        }

        ScaleType.FIT_XY -> {
            ScaleFactorCompat(scaleX = widthScale, scaleY = heightScale)
        }

        ScaleType.MATRIX -> ScaleFactorCompat(1.0f, 1.0f)
        else -> ScaleFactorCompat(scaleX = 1.0f, scaleY = 1.0f)
    }
}

internal fun computeContentScaleOffset(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat,
    scaleType: ScaleType
): OffsetCompat {
    val scaleFactor = scaleType.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    val scaledSrcSize = srcSize.times(scaleFactor)
    return when (scaleType) {
        ScaleType.CENTER -> OffsetCompat(
            x = (dstSize.width - scaledSrcSize.width) / 2.0f,
            y = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.CENTER_CROP -> OffsetCompat(
            x = (dstSize.width - scaledSrcSize.width) / 2.0f,
            y = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.CENTER_INSIDE -> OffsetCompat(
            x = (dstSize.width - scaledSrcSize.width) / 2.0f,
            y = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.FIT_START -> OffsetCompat(
            x = 0.0f,
            y = 0.0f
        )

        ScaleType.FIT_CENTER -> OffsetCompat(
            x = (dstSize.width - scaledSrcSize.width) / 2.0f,
            y = (dstSize.height - scaledSrcSize.height) / 2.0f
        )

        ScaleType.FIT_END -> OffsetCompat(
            x = dstSize.width - scaledSrcSize.width.toFloat(),
            y = dstSize.height - scaledSrcSize.height.toFloat()
        )

        ScaleType.FIT_XY -> OffsetCompat(
            x = 0.0f,
            y = 0.0f
        )

        ScaleType.MATRIX -> OffsetCompat(
            x = 0.0f,
            y = 0.0f
        )

        else -> OffsetCompat(
            x = 0.0f,
            y = 0.0f
        )
    }
}

internal fun ScaleType.supportReadMode(): Boolean = this != ScaleType.FIT_XY

internal fun computeReadModeTransform(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat,
    scaleType: ScaleType,
): TransformCompat {
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

internal fun computeContentInContainerInnerRect(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    scaleType: ScaleType,
): Rect {
    if (containerSize.isEmpty() || contentSize.isEmpty()) return ZeroRect
    val contentScaleFactor =
        scaleType.computeScaleFactor(srcSize = contentSize, dstSize = containerSize)
    val contentScaledContentSize = contentSize.times(contentScaleFactor)
    val offset = computeContentScaleOffset(
        srcSize = contentSize,
        dstSize = containerSize,
        scaleType = scaleType,
    )
    return Rect(
        left = offset.x.coerceAtLeast(0f).roundToInt(),
        top = offset.y.coerceAtLeast(0f).roundToInt(),
        right = (offset.x + contentScaledContentSize.width).roundToInt()
            .coerceAtMost(containerSize.width),
        bottom = (offset.y + contentScaledContentSize.height).roundToInt()
            .coerceAtMost(containerSize.height),
    )
}


internal fun computeUserOffsetBounds(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    scaleType: ScaleType,
    userScale: Float
): Rect {
    // based on the top left zoom
    if (userScale <= 1.0f || containerSize.isEmpty() || contentSize.isEmpty()) {
        return ZeroRect
    }
    val scaledContainerSize = containerSize.times(userScale)
    val scaledContentInContainerInnerRect = computeContentInContainerInnerRect(
        containerSize = containerSize,
        contentSize = contentSize,
        scaleType = scaleType,
    ).scale(userScale)

    val horizontalBounds = if (scaledContentInContainerInnerRect.width() > containerSize.width) {
        ((scaledContentInContainerInnerRect.right - containerSize.width) * -1)..(scaledContentInContainerInnerRect.left * -1)
    } else if (scaleType.isStart(srcSize = contentSize, dstSize = containerSize)) {
        0..0
    } else if (scaleType.isHorizontalCenter(srcSize = contentSize, dstSize = containerSize)) {
        val horizontalSpace = (scaledContainerSize.width - containerSize.width) / 2 * -1
        horizontalSpace..horizontalSpace
    } else {   // contentAlignment.isEnd
        val horizontalSpace = (scaledContainerSize.width - containerSize.width) * -1
        horizontalSpace..horizontalSpace
    }

    val verticalBounds = if (scaledContentInContainerInnerRect.height() > containerSize.height) {
        ((scaledContentInContainerInnerRect.bottom - containerSize.height) * -1)..(scaledContentInContainerInnerRect.top * -1)
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


internal fun ScaleType.isStart(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.width < dstSize.width)
}

internal fun ScaleType.isHorizontalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.width >= dstSize.width)
            || (this == ScaleType.FIT_END && scaledSrcSize.width >= dstSize.width)
}

internal fun ScaleType.isCenter(
    @Suppress("UNUSED_PARAMETER") srcSize: IntSizeCompat,
    @Suppress("UNUSED_PARAMETER") dstSize: IntSizeCompat
): Boolean =
    this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER

internal fun ScaleType.isEnd(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.width < dstSize.width
}

internal fun ScaleType.isTop(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.MATRIX
            || this == ScaleType.FIT_XY
            || (this == ScaleType.FIT_START && scaledSrcSize.height < dstSize.height)
}

internal fun ScaleType.isVerticalCenter(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.CENTER
            || this == ScaleType.CENTER_CROP
            || this == ScaleType.CENTER_INSIDE
            || this == ScaleType.FIT_CENTER
            || (this == ScaleType.FIT_START && scaledSrcSize.height >= dstSize.height)
            || (this == ScaleType.FIT_END && scaledSrcSize.height >= dstSize.height)
}

internal fun ScaleType.isBottom(srcSize: IntSizeCompat, dstSize: IntSizeCompat): Boolean {
    val scaledSrcSize = srcSize.times(computeScaleFactor(srcSize = srcSize, dstSize = dstSize))
    return this == ScaleType.FIT_END && scaledSrcSize.height < dstSize.height
}
