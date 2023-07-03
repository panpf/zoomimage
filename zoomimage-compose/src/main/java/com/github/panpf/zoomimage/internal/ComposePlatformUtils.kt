package com.github.panpf.zoomimage.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

internal fun Size.toShortString(): String = if (isSpecified) "(${width},$height)" else "Unspecified"

internal fun Size.rotate(rotateDegrees: Int): Size {
    return if (rotateDegrees % 180 == 0) this else Size(height, width)
}

internal fun Offset.toShortString(): String =
    if (isSpecified) "(${x.format(1)},${y.format(1)})" else "Unspecified"

internal fun Rect.toShortString(): String =
    "(${left.format(1)},${top.format(1)} - ${right.format(1)},${bottom.format(1)})"


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


/**
 * Binary subtraction operator.
 *
 * Returns an offset whose [translationX] value is the left-hand-side operand's [translationX]
 * minus the right-hand-side operand's [translationX] and whose [translationY] value is the
 * left-hand-side operand's [translationY] minus the right-hand-side operand's [translationY].
 */
//    @Stable
operator fun Translation.minus(other: Offset): Translation =
    Translation(translationX - other.x, translationY - other.y)

/**
 * Binary addition operator.
 *
 * Returns an offset whose [translationX] value is the sum of the [translationX] values of the
 * two operands, and whose [translationY] value is the sum of the [translationY] values of the
 * two operands.
 */
//    @Stable
operator fun Translation.plus(other: Offset): Translation =
    Translation(translationX + other.x, translationY + other.y)

fun Translation.toOffset(): Offset = Offset(x = translationX, y = translationY)
