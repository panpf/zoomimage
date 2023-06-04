package com.github.panpf.zoomimage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.math.RoundingMode
import java.text.DecimalFormat

fun Size.toShortString(): String = "(${width}x$height)"

fun Offset.toShortString(): String = "(${x.format(1)}x${y.format(1)})"

fun Rect.toShortString(): String =
    "(${left.format(1)},${top.format(1)},${right.format(1)},${bottom.format(1)})"

fun Centroid.toShortString(): String = "(${x.format(1)}x${y.format(1)})"

val ContentScale.name: String
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

val Alignment.name: String
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

val Alignment.isStart: Boolean
    get() = this == Alignment.TopStart || this == Alignment.CenterStart || this == Alignment.BottomStart
val Alignment.isHorizontalCenter: Boolean
    get() = this == Alignment.TopCenter || this == Alignment.Center || this == Alignment.BottomCenter
val Alignment.isCenter: Boolean
    get() = this == Alignment.Center
val Alignment.isEnd: Boolean
    get() = this == Alignment.TopEnd || this == Alignment.CenterEnd || this == Alignment.BottomEnd
val Alignment.isTop: Boolean
    get() = this == Alignment.TopStart || this == Alignment.TopCenter || this == Alignment.TopEnd
val Alignment.isVerticalCenter: Boolean
    get() = this == Alignment.CenterStart || this == Alignment.Center || this == Alignment.CenterEnd
val Alignment.isBottom: Boolean
    get() = this == Alignment.BottomStart || this == Alignment.BottomCenter || this == Alignment.BottomEnd

/* ******************************************* Double and Float *******************************************/
fun Float.format(
    decimalPlacesLength: Int = 2,
    decimalPlacesFillZero: Boolean = false,
    suffix: String? = null,
): String {
    val value = this
    val buffString = StringBuilder()
    buffString.append("#")
    if (decimalPlacesLength > 0) {
        buffString.append(".")
        for (w in 0 until decimalPlacesLength) {
            buffString.append(if (decimalPlacesFillZero) "0" else "#")
        }
    }
    val format = DecimalFormat(buffString.toString())
    format.roundingMode = RoundingMode.HALF_UP
    return if (suffix == null) {
        format.format(value)
    } else {
        format.format(value) + suffix
    }
}

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@Composable
fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}
