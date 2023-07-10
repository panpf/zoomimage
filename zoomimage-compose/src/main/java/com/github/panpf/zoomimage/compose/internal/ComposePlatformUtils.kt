package com.github.panpf.zoomimage.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun Size.isAvailable(): Boolean = isSpecified && !isEmpty()

internal fun Size.isNotAvailable(): Boolean = isUnspecified || isEmpty()

internal fun Size.toShortString(): String = if (isSpecified) "(${width},$height)" else "Unspecified"

internal fun Size.rotate(rotateDegrees: Int): Size {
    return if (rotateDegrees % 180 == 0) this else Size(height, width)
}

internal fun Offset.toShortString(): String =
    if (isSpecified) "(${x.format(1)},${y.format(1)})" else "Unspecified"
/**
 * Rotates the given offset around the origin by the given angle in degrees.
 *
 * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
 * coordinate system.
 *
 * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
 */
fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}

internal fun Rect.toShortString(): String =
    "(${left.format(1)},${top.format(1)} - ${right.format(1)},${bottom.format(1)})"

internal fun ScaleFactor.toShortString(): String = "(${scaleX.format(2)},${scaleY.format(2)})"

@Stable
internal operator fun ScaleFactor.times(scaleFactor: ScaleFactor) =
    ScaleFactor(scaleX * scaleFactor.scaleX, scaleY * scaleFactor.scaleY)

@Stable
internal operator fun ScaleFactor.div(scaleFactor: ScaleFactor) =
    ScaleFactor(scaleX / scaleFactor.scaleX, scaleY / scaleFactor.scaleY)

internal fun ScaleFactor(scale: Float): ScaleFactor = ScaleFactor(scale, scale)


internal fun Rect.scale(scale: Float): Rect {
    return Rect(
        left = (left * scale),
        top = (top * scale),
        right = (right * scale),
        bottom = (bottom * scale)
    )
}

internal fun Rect.restoreScale(scale: Float): Rect {
    return Rect(
        left = (left / scale),
        top = (top / scale),
        right = (right / scale),
        bottom = (bottom / scale)
    )
}

internal fun Rect.restoreScale(scaleFactor: ScaleFactor): Rect {
    return Rect(
        left = (left / scaleFactor.scaleX),
        top = (top / scaleFactor.scaleY),
        right = (right / scaleFactor.scaleX),
        bottom = (bottom / scaleFactor.scaleY)
    )
}

internal val ContentScale.name: String
    get() = when (this) {
        ContentScale.FillWidth -> "FillWidth"
        ContentScale.FillHeight -> "FillHeight"
        ContentScale.FillBounds -> "FillBounds"
        ContentScale.Fit -> "Fit"
        ContentScale.Crop -> "Crop"
        ContentScale.Inside -> "Inside"
        ContentScale.None -> "None"
        else -> "Unknown"
    }

internal val Alignment.name: String
    get() = when (this) {
        Alignment.TopStart -> "TopStart"
        Alignment.TopCenter -> "TopCenter"
        Alignment.TopEnd -> "TopEnd"
        Alignment.CenterStart -> "CenterStart"
        Alignment.Center -> "Center"
        Alignment.CenterEnd -> "CenterEnd"
        Alignment.BottomStart -> "BottomStart"
        Alignment.BottomCenter -> "BottomCenter"
        Alignment.BottomEnd -> "BottomEnd"
        else -> "Unknown"
    }

internal val Alignment.isStart: Boolean
    get() = this == Alignment.TopStart || this == Alignment.CenterStart || this == Alignment.BottomStart
internal val Alignment.isHorizontalCenter: Boolean
    get() = this == Alignment.TopCenter || this == Alignment.Center || this == Alignment.BottomCenter
internal val Alignment.isCenter: Boolean
    get() = this == Alignment.Center
internal val Alignment.isEnd: Boolean
    get() = this == Alignment.TopEnd || this == Alignment.CenterEnd || this == Alignment.BottomEnd
internal val Alignment.isTop: Boolean
    get() = this == Alignment.TopStart || this == Alignment.TopCenter || this == Alignment.TopEnd
internal val Alignment.isVerticalCenter: Boolean
    get() = this == Alignment.CenterStart || this == Alignment.Center || this == Alignment.CenterEnd
internal val Alignment.isBottom: Boolean
    get() = this == Alignment.BottomStart || this == Alignment.BottomCenter || this == Alignment.BottomEnd

@Composable
internal fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@Composable
internal fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}
