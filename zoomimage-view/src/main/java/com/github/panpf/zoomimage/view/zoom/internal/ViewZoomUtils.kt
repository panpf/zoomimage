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
@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.view.zoom.internal

import android.graphics.Rect
import android.graphics.RectF
import android.widget.ImageView.ScaleType
import com.github.panpf.zoomimage.ReadMode
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.InitialZoom
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.computeStepScales
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.minus
import com.github.panpf.zoomimage.util.rotate
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.view.internal.Rect
import com.github.panpf.zoomimage.view.internal.ZeroRect
import com.github.panpf.zoomimage.view.internal.computeScaleFactor
import com.github.panpf.zoomimage.view.internal.isHorizontalCenter
import com.github.panpf.zoomimage.view.internal.isStart
import com.github.panpf.zoomimage.view.internal.isTop
import com.github.panpf.zoomimage.view.internal.isVerticalCenter
import com.github.panpf.zoomimage.view.internal.scale
import com.github.panpf.zoomimage.view.internal.times
import com.github.panpf.zoomimage.view.internal.toContentScale
import kotlin.math.max
import kotlin.math.roundToInt

internal fun reverseRotateRect(rect: Rect, rotation: Int, drawableSize: IntSizeCompat) {
    require(rotation % 90 == 0) {
        "rotation must be an integer multiple of 90"
    }
    when (rotation % 360) {
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

internal fun IntOffsetCompat.rotateInContainer(
    containerSize: IntSizeCompat,
    rotate: Int,
): IntOffsetCompat {
    require(rotate % 90 == 0) { "rotate must be an integer multiple of 90" }
    return when (rotate) {
        90 -> {
            IntOffsetCompat(
                x = containerSize.height - y,
                y = x
            )
        }

        180 -> {
            IntOffsetCompat(
                x = containerSize.width - x,
                y = containerSize.height - y
            )
        }

        270 -> {
            IntOffsetCompat(
                x = y,
                y = containerSize.width - x
            )
        }

        else -> this
    }
}

internal fun ScaleType.computeTransform(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat
): TransformCompat {
    val scaleFactor = this.computeScaleFactor(srcSize, dstSize)
    val offset = computeContentScaleOffset(srcSize, dstSize, this)
    return TransformCompat(scale = scaleFactor, offset = offset.toOffset())
}

internal fun computeContentScaleOffset(
    srcSize: IntSizeCompat,
    dstSize: IntSizeCompat,
    scaleType: ScaleType
): IntOffsetCompat {
    val scaleFactor = scaleType.computeScaleFactor(srcSize = srcSize, dstSize = dstSize)
    val scaledSrcSize = srcSize.times(scaleFactor)
    val horSpace = ((dstSize.width - scaledSrcSize.width) / 2.0f).roundToInt()
    val verSpace = ((dstSize.height - scaledSrcSize.height) / 2.0f).roundToInt()
    return when (scaleType) {
        ScaleType.CENTER -> IntOffsetCompat(x = horSpace, y = verSpace)
        ScaleType.CENTER_CROP -> IntOffsetCompat(x = horSpace, y = verSpace)
        ScaleType.CENTER_INSIDE -> IntOffsetCompat(x = horSpace, y = verSpace)
        ScaleType.FIT_START -> IntOffsetCompat(x = 0, y = 0)
        ScaleType.FIT_CENTER -> IntOffsetCompat(x = horSpace, y = verSpace)
        ScaleType.FIT_END -> IntOffsetCompat(
            x = dstSize.width - scaledSrcSize.width,
            y = dstSize.height - scaledSrcSize.height
        )

        ScaleType.FIT_XY -> IntOffsetCompat(x = 0, y = 0)
        ScaleType.MATRIX -> IntOffsetCompat(x = 0, y = 0)
        else -> IntOffsetCompat(
            x = 0,
            y = 0
        )
    }
}

internal fun ScaleType.supportReadMode(): Boolean = this != ScaleType.FIT_XY


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
        left = offset.x.coerceAtLeast(0),
        top = offset.y.coerceAtLeast(0),
        right = (offset.x + contentScaledContentSize.width)
            .coerceAtMost(containerSize.width),
        bottom = (offset.y + contentScaledContentSize.height)
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

internal fun computeLocationOffset(
    rotatedOffsetOfContent: IntOffsetCompat,
    viewSize: IntSizeCompat,
    displayRectF: RectF,
    currentScale: ScaleFactorCompat
): IntOffsetCompat {
    val newX = rotatedOffsetOfContent.x
    val newY = rotatedOffsetOfContent.y
    val scaleLocationX = (newX * currentScale.scaleX).toInt()
    val scaleLocationY = (newY * currentScale.scaleY).toInt()
    val scaledLocationX =
        scaleLocationX.coerceIn(0, displayRectF.width().toInt())
    val scaledLocationY =
        scaleLocationY.coerceIn(0, displayRectF.height().toInt())
    return IntOffsetCompat(
        x = (scaledLocationX - viewSize.width / 2).coerceAtLeast(0),
        y = (scaledLocationY - viewSize.height / 2).coerceAtLeast(0)
    )
}

private fun computeInitialUserTransform(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentScale: ContentScaleCompat,
    rotation: Int,
    baseTransform: TransformCompat,
    readMode: ReadMode?,
): TransformCompat? {
    if (readMode == null) return null
    if (contentScale == ContentScaleCompat.FillBounds) return null
    val rotatedContentSize = contentSize.rotate(rotation)
    if (!readMode.accept(srcSize = rotatedContentSize, dstSize = containerSize)) return null

    val widthScale = containerSize.width / rotatedContentSize.width.toFloat()
    val heightScale = containerSize.height / rotatedContentSize.height.toFloat()
    val fillScale = max(widthScale, heightScale)
    val addScale = fillScale / baseTransform.scaleX
    val scaleX = baseTransform.scaleX * addScale
    val scaleY = baseTransform.scaleY * addScale
    val translateX = if (baseTransform.offset.x < 0)
        baseTransform.offset.x * addScale else 0f
    val translateY = if (baseTransform.offset.y < 0)
        baseTransform.offset.y * addScale else 0f
    val readModeTransform = TransformCompat(
        scale = ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY),
        offset = OffsetCompat(x = translateX, y = translateY)
    )
    val initialUserTransform = readModeTransform - baseTransform
    return initialUserTransform
}

internal fun computeInitialZoom(
    containerSize: IntSizeCompat,
    contentSize: IntSizeCompat,
    contentOriginSize: IntSizeCompat,
    scaleType: ScaleType,
    rotation: Int,
    readMode: ReadMode?,
    mediumScaleMinMultiple: Float,
): InitialZoom {
    if (contentSize.isEmpty() || containerSize.isEmpty()) {
        return InitialZoom.Origin
    }
    val rotatedContentSize = contentSize.rotate(rotation)

    val stepScales = computeStepScales(
        containerSize = containerSize,
        contentSize = contentSize,
        contentOriginSize = contentOriginSize,
        contentScale = scaleType.toContentScale(),
        rotation = rotation,
        mediumScaleMinMultiple = mediumScaleMinMultiple,
    )

    val baseTransform = scaleType
        .computeTransform(srcSize = rotatedContentSize, dstSize = containerSize)

    val userTransform = computeInitialUserTransform(
        containerSize = containerSize,
        contentSize = contentSize,
        contentScale = scaleType.toContentScale(),
        rotation = rotation,
        readMode = readMode,
        baseTransform = baseTransform,
    )

    return InitialZoom(
        minScale = stepScales[0],
        mediumScale = stepScales[1],
        maxScale = stepScales[2],
        baseTransform = baseTransform,
        userTransform = userTransform ?: TransformCompat.Origin
    )
}