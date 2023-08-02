package com.github.panpf.zoomimage.compose.internal

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
import com.github.panpf.zoomimage.util.ScaleMode
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import kotlin.math.roundToInt


fun ContentScale.toScaleMode(): ScaleMode = when (this) {
    ContentScale.Fit -> ScaleMode.FIT
    ContentScale.FillBounds -> ScaleMode.FILL_BOUNDS
    ContentScale.FillWidth -> ScaleMode.FILL_UNILATERAL
    ContentScale.FillHeight -> ScaleMode.FILL_UNILATERAL
    ContentScale.Crop -> ScaleMode.CROP
    ContentScale.Inside -> ScaleMode.INSIDE
    ContentScale.None -> ScaleMode.NONE
    else -> ScaleMode.NONE
}


fun Size.toCompat(): SizeCompat =
    takeIf { it.isSpecified }
        ?.let { SizeCompat(width = it.width, height = it.height) }
        ?: SizeCompat.Zero

fun Size.roundToCompat(): IntSizeCompat =
    takeIf { it.isSpecified }
        ?.let { IntSizeCompat(width = it.width.roundToInt(), height = it.height.roundToInt()) }
        ?: IntSizeCompat.Zero


fun IntSize.toCompat(): IntSizeCompat =
    IntSizeCompat(width = width, height = height)

fun IntSize.toCompatSize(): SizeCompat =
    SizeCompat(width = width.toFloat(), height = height.toFloat())


fun SizeCompat.toPlatform(): Size =
    Size(width = width, height = height)

fun SizeCompat.roundToPlatform(): IntSize =
    IntSize(width = width.roundToInt(), height = height.roundToInt())


fun IntSizeCompat.toPlatform(): IntSize =
    IntSize(width = width, height = height)

fun IntSizeCompat.toPlatformSize(): Size =
    Size(width = width.toFloat(), height = height.toFloat())


fun Rect.toCompat(): RectCompat =
    RectCompat(left = left, top = top, right = right, bottom = bottom)

fun Rect.roundToCompat(): IntRectCompat =
    IntRectCompat(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )


fun IntRect.toCompat(): IntRectCompat =
    IntRectCompat(left = left, top = top, right = right, bottom = bottom)

fun IntRect.toCompatRect(): RectCompat =
    RectCompat(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )


fun RectCompat.toPlatform(): Rect =
    Rect(left = left, top = top, right = right, bottom = bottom)

fun RectCompat.roundToPlatform(): IntRect =
    IntRect(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )


fun IntRectCompat.toPlatform(): IntRect =
    IntRect(left = left, top = top, right = right, bottom = bottom)

fun IntRectCompat.toPlatformRect(): Rect =
    Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )


fun Offset.toCompat(): OffsetCompat =
    takeIf { it.isSpecified }
        ?.let { OffsetCompat(x = it.x, y = it.y) }
        ?: OffsetCompat.Zero

fun Offset.roundToCompat(): IntOffsetCompat =
    takeIf { it.isSpecified }
        ?.let { IntOffsetCompat(x = it.x.roundToInt(), y = it.y.roundToInt()) }
        ?: IntOffsetCompat.Zero


fun IntOffset.toCompat(): IntOffsetCompat =
    IntOffsetCompat(x = x, y = y)

fun IntOffset.toCompatOffset(): OffsetCompat =
    OffsetCompat(x = x.toFloat(), y = y.toFloat())


fun OffsetCompat.toPlatform(): Offset = Offset(x = x, y = y)

fun OffsetCompat.roundToPlatform(): IntOffset = IntOffset(x = x.roundToInt(), y = y.roundToInt())


fun IntOffsetCompat.toPlatform(): IntOffset = IntOffset(x = x, y = y)

fun IntOffsetCompat.toPlatformOffset(): Offset = Offset(x = x.toFloat(), y = y.toFloat())


fun TransformOrigin.toCompatOrigin(): TransformOriginCompat {
    return TransformOriginCompat(pivotFractionX = pivotFractionX, pivotFractionY = pivotFractionY)
}

fun TransformOriginCompat.toTransformOrigin(): TransformOrigin {
    return TransformOrigin(pivotFractionX = pivotFractionX, pivotFractionY = pivotFractionY)
}


fun ScaleFactor.toCompatScaleFactor(): ScaleFactorCompat {
    return ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY)
}

fun ScaleFactorCompat.toScaleFactor(): ScaleFactor {
    return ScaleFactor(scaleX = scaleX, scaleY = scaleY)
}


fun Transform.toCompatTransform(): TransformCompat {
    return TransformCompat(
        scale = scale.toCompatScaleFactor(),
        offset = offset.toCompat(),
        rotation = rotation,
        scaleOrigin = scaleOrigin.toCompatOrigin(),
        rotationOrigin = rotationOrigin.toCompatOrigin(),
    )
}

fun TransformCompat.toTransform(): Transform {
    return Transform(
        scale = scale.toScaleFactor(),
        offset = offset.toPlatform(),
        rotation = rotation,
        scaleOrigin = scaleOrigin.toTransformOrigin(),
        rotationOrigin = rotationOrigin.toTransformOrigin(),
    )
}
