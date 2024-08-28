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

package com.github.panpf.zoomimage.compose.util

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.isSpecified
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import kotlin.math.roundToInt


/**
 * Convert [ContentScale] to [ContentScaleCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testContentScaleToCompat
 */
fun ContentScale.toCompat(): ContentScaleCompat = when (this) {
    ContentScale.Fit -> ContentScaleCompat.Fit
    ContentScale.FillBounds -> ContentScaleCompat.FillBounds
    ContentScale.FillWidth -> ContentScaleCompat.FillWidth
    ContentScale.FillHeight -> ContentScaleCompat.FillHeight
    ContentScale.Crop -> ContentScaleCompat.Crop
    ContentScale.Inside -> ContentScaleCompat.Inside
    ContentScale.None -> ContentScaleCompat.None
    else -> throw IllegalArgumentException("Unsupported ContentScale: $this")
}

/**
 * Convert [ContentScaleCompat] to [ContentScale]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testContentScaleCompatToPlatform
 */
fun ContentScaleCompat.toPlatform(): ContentScale = when (this) {
    ContentScaleCompat.Fit -> ContentScale.Fit
    ContentScaleCompat.FillBounds -> ContentScale.FillBounds
    ContentScaleCompat.FillWidth -> ContentScale.FillWidth
    ContentScaleCompat.FillHeight -> ContentScale.FillHeight
    ContentScaleCompat.Crop -> ContentScale.Crop
    ContentScaleCompat.Inside -> ContentScale.Inside
    ContentScaleCompat.None -> ContentScale.None
    else -> throw IllegalArgumentException("Unsupported ContentScale: $this")
}

/**
 * Convert [Alignment] to [AlignmentCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testAlignmentToCompat
 */
fun Alignment.toCompat(): AlignmentCompat = when (this) {
    Alignment.TopStart -> AlignmentCompat.TopStart
    Alignment.TopCenter -> AlignmentCompat.TopCenter
    Alignment.TopEnd -> AlignmentCompat.TopEnd
    Alignment.CenterStart -> AlignmentCompat.CenterStart
    Alignment.Center -> AlignmentCompat.Center
    Alignment.CenterEnd -> AlignmentCompat.CenterEnd
    Alignment.BottomStart -> AlignmentCompat.BottomStart
    Alignment.BottomCenter -> AlignmentCompat.BottomCenter
    Alignment.BottomEnd -> AlignmentCompat.BottomEnd
    else -> throw IllegalArgumentException("Unsupported Alignment: $this")
}

/**
 * Convert [AlignmentCompat] to [Alignment]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testAlignmentCompatToPlatform
 */
fun AlignmentCompat.toPlatform(): Alignment = when (this) {
    AlignmentCompat.TopStart -> Alignment.TopStart
    AlignmentCompat.TopCenter -> Alignment.TopCenter
    AlignmentCompat.TopEnd -> Alignment.TopEnd
    AlignmentCompat.CenterStart -> Alignment.CenterStart
    AlignmentCompat.Center -> Alignment.Center
    AlignmentCompat.CenterEnd -> Alignment.CenterEnd
    AlignmentCompat.BottomStart -> Alignment.BottomStart
    AlignmentCompat.BottomCenter -> Alignment.BottomCenter
    AlignmentCompat.BottomEnd -> Alignment.BottomEnd
    else -> throw IllegalArgumentException("Unsupported Alignment: $this")
}


/**
 * Convert [Size] to [SizeCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testSizeToCompat
 */
fun Size.toCompat(): SizeCompat =
    if (isSpecified) {
        SizeCompat(width = width, height = height)
    } else {
        SizeCompat.Unspecified
    }

/**
 * Convert [Size] to [IntSizeCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testSizeRoundToCompat
 */
fun Size.roundToCompat(): IntSizeCompat =
    if (isSpecified) {
        IntSizeCompat(width = width.roundToInt(), height = height.roundToInt())
    } else {
        IntSizeCompat.Zero
    }


/**
 * Convert [IntSize] to [IntSizeCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntSizeToCompat
 */
fun IntSize.toCompat(): IntSizeCompat =
    IntSizeCompat(width = width, height = height)

/**
 * Convert [IntSize] to [SizeCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntSizeToCompatSize
 */
fun IntSize.toCompatSize(): SizeCompat =
    SizeCompat(width = width.toFloat(), height = height.toFloat())


/**
 * Convert [SizeCompat] to [Size]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testSizeCompatToPlatform
 */
fun SizeCompat.toPlatform(): Size =
    if (isSpecified) {
        Size(width = width, height = height)
    } else {
        Size.Unspecified
    }

/**
 * Convert [SizeCompat] to [Size]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testSizeCompatRoundToPlatform
 */
fun SizeCompat.roundToPlatform(): IntSize =
    if (isSpecified) {
        IntSize(width = width.roundToInt(), height = height.roundToInt())
    } else {
        IntSize.Zero
    }


/**
 * Convert [IntSizeCompat] to [IntSize]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntSizeCompatToPlatform
 */
fun IntSizeCompat.toPlatform(): IntSize =
    IntSize(width = width, height = height)

/**
 * Convert [IntSizeCompat] to [Size]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntSizeCompatToPlatformSize
 */
fun IntSizeCompat.toPlatformSize(): Size =
    Size(width = width.toFloat(), height = height.toFloat())


/**
 * Convert [Rect] to [RectCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testRectToCompat
 */
fun Rect.toCompat(): RectCompat =
    RectCompat(left = left, top = top, right = right, bottom = bottom)

/**
 * Convert [Rect] to [IntRectCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testRectRoundToCompat
 */
fun Rect.roundToCompat(): IntRectCompat =
    IntRectCompat(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )


/**
 * Convert [IntRect] to [IntRectCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntRectToCompat
 */
fun IntRect.toCompat(): IntRectCompat =
    IntRectCompat(left = left, top = top, right = right, bottom = bottom)

/**
 * Convert [IntRect] to [RectCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntRectToCompatRect
 */
fun IntRect.toCompatRect(): RectCompat =
    RectCompat(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )


/**
 * Convert [RectCompat] to [Rect]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testRectCompatToPlatform
 */
fun RectCompat.toPlatform(): Rect =
    Rect(left = left, top = top, right = right, bottom = bottom)

/**
 * Convert [RectCompat] to [IntRect]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testRectCompatRoundToPlatform
 */
fun RectCompat.roundToPlatform(): IntRect =
    IntRect(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )


/**
 * Convert [IntRectCompat] to [IntRect]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntRectCompatToPlatform
 */
fun IntRectCompat.toPlatform(): IntRect =
    IntRect(left = left, top = top, right = right, bottom = bottom)

/**
 * Convert [IntRectCompat] to [Rect]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntRectCompatToPlatformRect
 */
fun IntRectCompat.toPlatformRect(): Rect =
    Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )


/**
 * Convert [Offset] to [OffsetCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testOffsetToCompat
 */
fun Offset.toCompat(): OffsetCompat =
    if (isSpecified) {
        OffsetCompat(x = x, y = y)
    } else {
        OffsetCompat.Unspecified
    }

/**
 * Convert [Offset] to [IntOffsetCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testOffsetRoundToCompat
 */
fun Offset.roundToCompat(): IntOffsetCompat =
    if (isSpecified) {
        IntOffsetCompat(x = x.roundToInt(), y = y.roundToInt())
    } else {
        IntOffsetCompat.Zero
    }


/**
 * Convert [IntOffset] to [IntOffsetCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntOffsetToCompat
 */
fun IntOffset.toCompat(): IntOffsetCompat =
    IntOffsetCompat(x = x, y = y)

/**
 * Convert [IntOffset] to [OffsetCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntOffsetToCompatOffset
 */
fun IntOffset.toCompatOffset(): OffsetCompat =
    OffsetCompat(x = x.toFloat(), y = y.toFloat())


/**
 * Convert [OffsetCompat] to [Offset]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testOffsetCompatToPlatform
 */
fun OffsetCompat.toPlatform(): Offset =
    if (isSpecified) {
        Offset(x = x, y = y)
    } else {
        Offset.Unspecified
    }

/**
 * Convert [OffsetCompat] to [Offset]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testOffsetCompatRoundToPlatform
 */
fun OffsetCompat.roundToPlatform(): IntOffset =
    if (isSpecified) {
        IntOffset(x = x.roundToInt(), y = y.roundToInt())
    } else {
        IntOffset.Zero
    }


/**
 * Convert [IntOffsetCompat] to [IntOffset]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntOffsetCompatToPlatform
 */
fun IntOffsetCompat.toPlatform(): IntOffset = IntOffset(x = x, y = y)

/**
 * Convert [IntOffsetCompat] to [Offset]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testIntOffsetCompatToPlatformOffset
 */
fun IntOffsetCompat.toPlatformOffset(): Offset = Offset(x = x.toFloat(), y = y.toFloat())


/**
 * Convert [TransformOrigin] to [TransformOriginCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testTransformOriginToCompat
 */
fun TransformOrigin.toCompat(): TransformOriginCompat {
    return TransformOriginCompat(pivotFractionX = pivotFractionX, pivotFractionY = pivotFractionY)
}

/**
 * Convert [TransformOriginCompat] to [TransformOrigin]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testTransformOriginCompatToPlatform
 */
fun TransformOriginCompat.toPlatform(): TransformOrigin {
    return TransformOrigin(pivotFractionX = pivotFractionX, pivotFractionY = pivotFractionY)
}


/**
 * Convert [ScaleFactor] to [ScaleFactorCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testScaleFactorToCompat
 */
fun ScaleFactor.toCompat(): ScaleFactorCompat {
    return ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY)
}

/**
 * Convert [ScaleFactorCompat] to [ScaleFactor]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testScaleFactorCompatToPlatform
 */
fun ScaleFactorCompat.toPlatform(): ScaleFactor {
    return ScaleFactor(scaleX = scaleX, scaleY = scaleY)
}


/**
 * Convert [Transform] to [TransformCompat]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testTransformToCompat
 */
fun Transform.toCompat(): TransformCompat {
    return TransformCompat(
        scale = scale.toCompat(),
        offset = offset.toCompat(),
        rotation = rotation,
        scaleOrigin = scaleOrigin.toCompat(),
        rotationOrigin = rotationOrigin.toCompat(),
    )
}

/**
 * Convert [TransformCompat] to [Transform]
 *
 * @see com.github.panpf.zoomimage.compose.common.test.util.ComposeConvertUtilsTest.testTransformCompatToPlatform
 */
fun TransformCompat.toPlatform(): Transform {
    return Transform(
        scale = scale.toPlatform(),
        offset = offset.toPlatform(),
        rotation = rotation,
        scaleOrigin = scaleOrigin.toPlatform(),
        rotationOrigin = rotationOrigin.toPlatform(),
    )
}