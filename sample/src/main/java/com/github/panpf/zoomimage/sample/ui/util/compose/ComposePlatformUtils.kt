package com.github.panpf.zoomimage.sample.ui.util.compose

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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.sample.util.format
import kotlin.math.roundToInt

internal fun Size.toShortString(): String =
    if (isSpecified) "${width.format(2)}x${height.format(2)}" else "Unspecified"

internal fun Offset.toShortString(): String =
    if (isSpecified) "${x.format(2)}x${y.format(2)}" else "Unspecified"

internal fun androidx.compose.ui.geometry.Rect.toShortString(): String =
    "${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}"

internal fun ScaleFactor.toShortString(): String = "${scaleX.format(2)}x${scaleY.format(2)}"

internal fun IntSize.toShortString(): String = "${width}x${height}"

internal fun IntOffset.toShortString(): String = "${x}x${y}"

internal fun IntRect.toShortString(): String = "${left}x${top},${right}x${bottom}"


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

internal fun IntRect.scale(scale: ScaleFactor): IntRect {
    return IntRect(
        left = (left * scale.scaleX).roundToInt(),
        top = (top * scale.scaleY).roundToInt(),
        right = (right * scale.scaleX).roundToInt(),
        bottom = (bottom * scale.scaleY).roundToInt(),
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

internal fun contentScale(name: String): ContentScale {
    return when (name) {
        "FillWidth" -> ContentScale.FillWidth
        "FillHeight" -> ContentScale.FillHeight
        "FillBounds" -> ContentScale.FillBounds
        "Fit" -> ContentScale.Fit
        "Crop" -> ContentScale.Crop
        "Inside" -> ContentScale.Inside
        "None" -> ContentScale.None
        else -> throw IllegalArgumentException("Unknown ContentScale name: $name")
    }
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

internal fun alignment(name: String): Alignment {
    return when (name) {
        "TopStart" -> Alignment.TopStart
        "TopCenter" -> Alignment.TopCenter
        "TopEnd" -> Alignment.TopEnd
        "CenterStart" -> Alignment.CenterStart
        "Center" -> Alignment.Center
        "CenterEnd" -> Alignment.CenterEnd
        "BottomStart" -> Alignment.BottomStart
        "BottomCenter" -> Alignment.BottomCenter
        "BottomEnd" -> Alignment.BottomEnd
        else -> throw IllegalArgumentException("Unknown alignment name: $name")
    }
}


@Composable
internal fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@Composable
internal fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}