package com.github.panpf.zoomimage.compose.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.core.IntOffsetCompat
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.RectCompat
import com.github.panpf.zoomimage.core.ScaleFactorCompat
import com.github.panpf.zoomimage.core.SizeCompat
import com.github.panpf.zoomimage.core.TransformCompat
import com.github.panpf.zoomimage.core.internal.ScaleMode
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


fun Size.roundToCompatIntSize(): IntSizeCompat {
    return takeIf { it.isSpecified }
        ?.let { IntSizeCompat(width = it.width.roundToInt(), height = it.height.roundToInt()) }
        ?: IntSizeCompat.Zero
}

fun Size.toCompatSize(): SizeCompat {
    return takeIf { it.isSpecified }
        ?.let { SizeCompat(width = it.width, height = it.height) }
        ?: SizeCompat.Zero
}

fun IntSize.toCompatIntSize(): IntSizeCompat {
    return IntSizeCompat(width = width, height = height)
}

fun IntSize.toCompatSize(): SizeCompat {
    return SizeCompat(width = width.toFloat(), height = height.toFloat())
}

fun SizeCompat.roundToIntSize(): IntSize {
    return IntSize(width = width.roundToInt(), height = height.roundToInt())
}

fun SizeCompat.toSize(): Size {
    return Size(width = width, height = height)
}

fun IntSizeCompat.toSize(): Size {
    return Size(width = width.toFloat(), height = height.toFloat())
}

fun IntSizeCompat.toIntSize(): IntSize {
    return IntSize(width = width, height = height)
}


fun Rect.roundToCompatIntRect(): IntRectCompat {
    return IntRectCompat(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )
}

fun Rect.toCompatRect(): RectCompat {
    return RectCompat(left = left, top = top, right = right, bottom = bottom)
}

fun IntRect.roundToCompatIntRect(): IntRectCompat {
    return IntRectCompat(left = left, top = top, right = right, bottom = bottom)
}

fun IntRect.toCompatRect(): RectCompat {
    return RectCompat(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
}

fun RectCompat.roundToIntRect(): IntRect {
    return IntRect(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )
}

fun RectCompat.toRect(): Rect {
    return Rect(left = left, top = top, right = right, bottom = bottom)
}

fun IntRectCompat.toRect(): Rect {
    return Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
}

fun IntRectCompat.toIntRect(): IntRect {
    return IntRect(left = left, top = top, right = right, bottom = bottom)
}


fun Offset.roundToCompatIntOffset(): IntOffsetCompat =
    takeIf { it.isSpecified }
        ?.let { IntOffsetCompat(x = it.x.roundToInt(), y = it.y.roundToInt()) }
        ?: IntOffsetCompat.Zero

fun Offset.toCompatOffset(): OffsetCompat =
    takeIf { it.isSpecified }
        ?.let { OffsetCompat(x = it.x, y = it.y) }
        ?: OffsetCompat.Zero

fun IntOffset.toCompatIntOffset(): IntOffsetCompat =
    IntOffsetCompat(x = x, y = y)

fun IntOffset.toCompatOffset(): OffsetCompat =
    OffsetCompat(x = x.toFloat(), y = y.toFloat())

fun OffsetCompat.roundToIntOffset(): IntOffset = IntOffset(x = x.roundToInt(), y = y.roundToInt())

fun OffsetCompat.toOffset(): Offset = Offset(x = x, y = y)

fun IntOffsetCompat.toIntOffset(): IntOffset = IntOffset(x = x, y = y)

fun IntOffsetCompat.toOffset(): Offset = Offset(x = x.toFloat(), y = y.toFloat())


fun ScaleFactor.toCompatScaleFactor(): ScaleFactorCompat {
    return ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY)
}

fun ScaleFactorCompat.toScaleFactor(): ScaleFactor {
    return ScaleFactor(scaleX = scaleX, scaleY = scaleY)
}


fun Transform.toCompatTransform(): TransformCompat {
    return TransformCompat(
        scale = scale.toCompatScaleFactor(),
        offset = offset.toCompatOffset(),
        rotation = rotation,
    )
}

fun TransformCompat.toTransform(): Transform {
    return Transform(
        scale = scale.toScaleFactor(),
        offset = offset.toOffset(),
        rotation = rotation,
    )
}
