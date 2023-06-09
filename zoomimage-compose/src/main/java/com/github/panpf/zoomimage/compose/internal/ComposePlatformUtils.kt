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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal fun Size.toShortString(): String =
    if (isSpecified) "${width.format(2)}x${height.format(2)}" else "Unspecified"

internal fun Offset.toShortString(): String =
    if (isSpecified) "${x.format(2)}x${y.format(2)}" else "Unspecified"

internal fun Rect.toShortString(): String =
    "${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}"

internal fun ScaleFactor.toShortString(): String = "${scaleX.format(2)}x${scaleY.format(2)}"

internal fun IntSize.toShortString(): String = "${width}x${height}"

internal fun IntOffset.toShortString(): String = "${x}x${y}"

internal fun IntRect.toShortString(): String = "${left}x${top},${right}x${bottom}"


internal fun Size.isAvailable(): Boolean = isSpecified && !isEmpty()

internal fun Size.isNotAvailable(): Boolean = isUnspecified || isEmpty()

internal fun Size.rotate(rotateDegrees: Int): Size {
    return if (rotateDegrees % 180 == 0) this else Size(height, width)
}

internal fun Size.roundToIntSize(): IntSize {
    return IntSize(width.roundToInt(), height.roundToInt())
}

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
        bottom = (bottom * scale),
    )
}

internal fun IntRect.scale(scale: Float): IntRect {
    return IntRect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt(),
    )
}

internal fun Rect.restoreScale(scale: Float): Rect {
    return Rect(
        left = (left / scale),
        top = (top / scale),
        right = (right / scale),
        bottom = (bottom / scale),
    )
}

internal fun Rect.restoreScale(scaleFactor: ScaleFactor): Rect {
    return Rect(
        left = (left / scaleFactor.scaleX),
        top = (top / scaleFactor.scaleY),
        right = (right / scaleFactor.scaleX),
        bottom = (bottom / scaleFactor.scaleY),
    )
}

internal fun IntRect.restoreScale(scale: Float): IntRect {
    return IntRect(
        left = (left / scale).roundToInt(),
        top = (top / scale).roundToInt(),
        right = (right / scale).roundToInt(),
        bottom = (bottom / scale).roundToInt(),
    )
}

internal fun IntRect.restoreScale(scaleFactor: ScaleFactor): IntRect {
    return IntRect(
        left = (left / scaleFactor.scaleX).roundToInt(),
        top = (top / scaleFactor.scaleY).roundToInt(),
        right = (right / scaleFactor.scaleX).roundToInt(),
        bottom = (bottom / scaleFactor.scaleY).roundToInt(),
    )
}

internal fun IntRect.limitTo(rect: IntRect): IntRect {
    return IntRect(
        left = left.coerceAtLeast(rect.left),
        top = top.coerceAtLeast(rect.top),
        right = right.coerceIn(rect.left, rect.right),
        bottom = bottom.coerceIn(rect.top, rect.bottom),
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

/**
 * Multiplication operator with [Size].
 *
 * Return a new [Size] with the width and height multiplied by the [ScaleFactor.scaleX] and
 * [ScaleFactor.scaleY] respectively
 */
@Stable
operator fun IntSize.times(scaleFactor: ScaleFactor): IntSize =
    IntSize(
        width = (this.width * scaleFactor.scaleX).roundToInt(),
        height = (this.height * scaleFactor.scaleY).roundToInt()
    )

/**
 * Multiplication operator with [Size].
 *
 * Return a new [Size] with the width and height multiplied by the [ScaleFactor.scaleX] and
 * [ScaleFactor.scaleY] respectively
 */
@Stable
operator fun IntSize.times(scale: Float): IntSize =
    IntSize(
        width = (this.width * scale).roundToInt(),
        height = (this.height * scale).roundToInt()
    )

@Stable
fun IntSize.isEmpty(): Boolean = width == 0 || height == 0

@Stable
fun IntSize.isNotEmpty(): Boolean = !isEmpty()

internal fun IntSize.rotate(rotateDegrees: Int): IntSize {
    return if (rotateDegrees % 180 == 0) this else IntSize(height, width)
}
