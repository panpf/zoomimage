package com.github.panpf.zoomimage.compose.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.core.OffsetCompat
import com.github.panpf.zoomimage.core.RectFCompat
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

fun Size.toCompatSize(): SizeCompat {
    return takeIf { it.isSpecified }
        ?.let { SizeCompat(it.width.roundToInt(), it.height.roundToInt()) }
        ?: SizeCompat.Empty
}

fun SizeCompat.toSize(): Size {
    return Size(width.toFloat(), height.toFloat())
}


fun Rect.toCompatRectF(): RectFCompat {
    return RectFCompat(left = left, top = top, right = right, bottom = bottom)
}

fun RectFCompat.toRect(): Rect {
    return Rect(left = left, top = top, right = right, bottom = bottom)
}


fun ScaleFactor.toCompatScaleFactor(): ScaleFactorCompat {
    return ScaleFactorCompat(scaleX = scaleX, scaleY = scaleY)
}

fun ScaleFactorCompat.toScaleFactor(): ScaleFactor {
    return ScaleFactor(scaleX = scaleX, scaleY = scaleY)
}


fun Offset.toCompatOffset(): OffsetCompat =
    takeIf { it.isSpecified }
        ?.let { OffsetCompat(it.x, it.y) }
        ?: OffsetCompat.Zero

fun OffsetCompat.toOffset(): Offset = Offset(x = x, y = y)

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
