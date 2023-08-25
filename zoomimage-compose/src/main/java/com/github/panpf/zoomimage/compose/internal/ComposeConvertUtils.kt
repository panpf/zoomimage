package com.github.panpf.zoomimage.compose.internal

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
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import kotlin.math.roundToInt


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


fun TransformOrigin.toCompat(): TransformOriginCompat {
    return TransformOriginCompat(pivotFractionX = pivotFractionX, pivotFractionY = pivotFractionY)
}

fun TransformOriginCompat.toPlatform(): TransformOrigin {
    return TransformOrigin(pivotFractionX = pivotFractionX, pivotFractionY = pivotFractionY)
}


fun ScaleFactor.toCompat(): ScaleFactorCompat {
    return ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY)
}

fun ScaleFactorCompat.toPlatform(): ScaleFactor {
    return ScaleFactor(scaleX = scaleX, scaleY = scaleY)
}


fun Transform.toCompat(): TransformCompat {
    return TransformCompat(
        scale = scale.toCompat(),
        offset = offset.toCompat(),
        rotation = rotation,
        scaleOrigin = scaleOrigin.toCompat(),
        rotationOrigin = rotationOrigin.toCompat(),
    )
}

fun TransformCompat.toPlatform(): Transform {
    return Transform(
        scale = scale.toPlatform(),
        offset = offset.toPlatform(),
        rotation = rotation,
        scaleOrigin = scaleOrigin.toPlatform(),
        rotationOrigin = rotationOrigin.toPlatform(),
    )
}
