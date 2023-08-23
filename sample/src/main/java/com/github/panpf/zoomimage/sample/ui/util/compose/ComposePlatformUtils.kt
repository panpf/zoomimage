package com.github.panpf.zoomimage.sample.ui.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.sample.util.format
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
internal fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@Composable
internal fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}


/* ****************************************** Size ********************************************** */

@Stable
internal fun Size.toShortString(): String =
    if (isSpecified) "${width.format(2)}x${height.format(2)}" else "Unspecified"

@Stable
internal fun Size.isNotEmpty(): Boolean = width > 0f && height > 0f

@Stable
internal fun Size.isSpecifiedAndNotEmpty(): Boolean = isSpecified && isNotEmpty()

@Stable
internal fun Size.isUnspecifiedOrEmpty(): Boolean = isUnspecified || isEmpty()

@Stable
internal fun Size.round(): IntSize =
    if (isSpecified) IntSize(width.roundToInt(), height.roundToInt()) else IntSize.Zero

@Stable
internal fun Size.toOffset(): Offset =
    if (isSpecified) Offset(x = width, y = height) else Offset.Unspecified

@Stable
internal fun Size.roundToOffset(): IntOffset =
    if (isSpecified) IntOffset(x = width.roundToInt(), y = height.roundToInt()) else IntOffset.Zero

@Stable
internal fun Size.rotate(rotation: Int): Size =
    if (rotation % 180 == 0) this else Size(height, width)

@Stable
internal fun Size.isSameAspectRatio(other: Size, delta: Float = 0f): Boolean {
    val selfScale = this.width / this.height
    val otherScale = other.width / other.height
    if (selfScale.compareTo(otherScale) == 0) {
        return true
    }
    if (delta != 0f && abs(selfScale - otherScale) <= delta) {
        return true
    }
    return false
}


/* **************************************** IntSize ********************************************* */

@Stable
internal fun IntSize.toShortString(): String = "${width}x${height}"

@Stable
internal fun IntSize.isEmpty(): Boolean = width <= 0 || height <= 0

@Stable
internal fun IntSize.isNotEmpty(): Boolean = width > 0 && height > 0

@Stable
internal operator fun IntSize.times(scaleFactor: ScaleFactor): IntSize =
    IntSize(
        width = (this.width * scaleFactor.scaleX).roundToInt(),
        height = (this.height * scaleFactor.scaleY).roundToInt()
    )

@Stable
internal operator fun IntSize.div(scaleFactor: ScaleFactor): IntSize =
    IntSize(
        width = (this.width / scaleFactor.scaleX).roundToInt(),
        height = (this.height / scaleFactor.scaleY).roundToInt()
    )

@Stable
internal operator fun IntSize.times(scale: Float): IntSize =
    IntSize(
        width = (this.width * scale).roundToInt(),
        height = (this.height * scale).roundToInt()
    )

@Stable
internal operator fun IntSize.div(scale: Float): IntSize =
    IntSize(
        width = (this.width / scale).roundToInt(),
        height = (this.height / scale).roundToInt()
    )

@Stable
internal fun IntSize.toOffset(): Offset = Offset(x = width.toFloat(), y = height.toFloat())

@Stable
internal fun IntSize.toIntOffset(): IntOffset = IntOffset(x = width, y = height)

@Stable
internal fun IntSize.rotate(rotation: Int): IntSize {
    return if (rotation % 180 == 0) this else IntSize(height, width)
}

@Stable
internal fun IntSize.isSameAspectRatio(other: IntSize, delta: Float = 0f): Boolean {
    val selfScale = this.width / this.height.toFloat()
    val otherScale = other.width / other.height.toFloat()
    if (selfScale.compareTo(otherScale) == 0) {
        return true
    }
    if (delta != 0f && abs(selfScale - otherScale) <= delta) {
        return true
    }
    return false
}

/**
 * Linearly interpolate between two [IntSize]s.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 */
@Stable
internal fun lerp(start: IntSize, stop: IntSize, fraction: Float): IntSize =
    IntSize(
        androidx.compose.ui.util.lerp(start.width, stop.width, fraction),
        androidx.compose.ui.util.lerp(start.height, stop.height, fraction)
    )

/**
 * Returns a copy of this IntOffset instance optionally overriding the
 * x or y parameter
 */
internal fun IntSize.copy(width: Int = this.width, height: Int = this.height) =
    IntSize(width = width, height = height)


/* ***************************************** Offset ********************************************* */

@Stable
internal fun Offset.toShortString(): String =
    if (isSpecified) "${x.format(2)}x${y.format(2)}" else "Unspecified"

@Stable
internal operator fun Offset.times(scaleFactor: ScaleFactor): Offset =
    Offset(x * scaleFactor.scaleX, y * scaleFactor.scaleY)

@Stable
internal operator fun Offset.div(scaleFactor: ScaleFactor): Offset =
    Offset(x = x / scaleFactor.scaleX, y = y / scaleFactor.scaleY)

@Stable
internal fun Offset.toSize(): Size =
    if (isSpecified) Size(width = x, height = y) else Size.Unspecified

@Stable
internal fun Offset.roundToSize(): IntSize =
    if (isSpecified) {
        IntSize(width = x.roundToInt(), height = y.roundToInt())
    } else {
        IntSize.Zero
    }

@Stable
internal fun Offset.rotateInSpace(spaceSize: Size, rotation: Int): Offset {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when (rotation % 360) {
        90 -> Offset(x = spaceSize.height - y, y = x)
        180 -> Offset(x = spaceSize.width - x, y = spaceSize.height - y)
        270 -> Offset(x = y, y = spaceSize.width - x)
        else -> this
    }
}

@Stable
internal fun Offset.reverseRotateInSpace(spaceSize: Size, rotation: Int): Offset {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = 360 - rotation % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

fun Offset.limitTo(size: Size): Offset {
    return if (x < 0f || x > size.width || y < 0f || y > size.height) {
        Offset(
            x = x.coerceIn(0f, size.width),
            y = y.coerceIn(0f, size.height),
        )
    } else {
        this
    }
}


/* ************************************** IntOffset ********************************************* */

@Stable
internal fun IntOffset.toShortString(): String = "${x}x${y}"

@Stable
internal operator fun IntOffset.times(scaleFactor: ScaleFactor): IntOffset =
    IntOffset(
        x = (x * scaleFactor.scaleX).roundToInt(),
        y = (y * scaleFactor.scaleY).roundToInt()
    )

@Stable
internal operator fun IntOffset.div(scaleFactor: ScaleFactor): IntOffset =
    IntOffset(
        x = (x / scaleFactor.scaleX).roundToInt(),
        y = (y / scaleFactor.scaleY).roundToInt()
    )

@Stable
internal fun IntOffset.toSize(): Size = Size(width = x.toFloat(), height = y.toFloat())

@Stable
internal fun IntOffset.toIntSize(): IntSize = IntSize(width = x, height = y)

@Stable
internal fun IntOffset.rotateInSpace(spaceSize: IntSize, rotation: Int): IntOffset {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when (rotation % 360) {
        90 -> IntOffset(x = spaceSize.height - y, y = x)
        180 -> IntOffset(x = spaceSize.width - x, y = spaceSize.height - y)
        270 -> IntOffset(x = y, y = spaceSize.width - x)
        else -> this
    }
}

@Stable
internal fun IntOffset.reverseRotateInSpace(spaceSize: IntSize, rotation: Int): IntOffset {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = 360 - rotation % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}

fun IntOffset.limitTo(size: IntSize): IntOffset {
    return if (x < 0 || x > size.width || y < 0 || y > size.height) {
        IntOffset(
            x = x.coerceIn(0, size.width),
            y = y.coerceIn(0, size.height),
        )
    } else {
        this
    }
}


/* ******************************************* Rect ********************************************* */

@Stable
internal fun Rect.toShortString(): String =
    "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"

@Stable
internal fun Rect.scale(scale: Float): Rect =
    Rect(
        left = (left * scale),
        top = (top * scale),
        right = (right * scale),
        bottom = (bottom * scale),
    )

@Stable
internal fun Rect.scale(scale: ScaleFactor): Rect =
    Rect(
        left = (left * scale.scaleX),
        top = (top * scale.scaleY),
        right = (right * scale.scaleX),
        bottom = (bottom * scale.scaleY),
    )

@Stable
internal fun Rect.restoreScale(scale: Float): Rect =
    Rect(
        left = (left / scale),
        top = (top / scale),
        right = (right / scale),
        bottom = (bottom / scale),
    )

@Stable
internal fun Rect.restoreScale(scaleFactor: ScaleFactor): Rect =
    Rect(
        left = (left / scaleFactor.scaleX),
        top = (top / scaleFactor.scaleY),
        right = (right / scaleFactor.scaleX),
        bottom = (bottom / scaleFactor.scaleY),
    )

@Stable
internal fun Rect.limitTo(rect: Rect): Rect =
    if (this.left < rect.left
        || this.top < rect.top
        || this.right < rect.left || this.right > rect.right
        || this.bottom < rect.top || this.bottom > rect.bottom
    ) {
        Rect(
            left = left.coerceAtLeast(rect.left),
            top = top.coerceAtLeast(rect.top),
            right = right.coerceIn(rect.left, rect.right),
            bottom = bottom.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }

@Stable
internal fun Rect.limitTo(size: Size): Rect =
    if (this.left < 0f
        || this.top < 0f
        || this.right < 0f || this.right > size.width
        || this.bottom < 0f || this.bottom > size.height
    ) {
        Rect(
            left = left.coerceAtLeast(0f),
            top = top.coerceAtLeast(0f),
            right = right.coerceIn(0f, size.width),
            bottom = bottom.coerceIn(0f, size.height),
        )
    } else {
        this
    }

@Stable
internal fun Rect.rotateInSpace(spaceSize: Size, rotation: Int): Rect {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when (rotation % 360) {
        90 -> {
            Rect(
                left = spaceSize.height - this.bottom,
                top = this.left,
                right = spaceSize.height - this.top,
                bottom = this.right
            )
        }

        180 -> {
            Rect(
                left = spaceSize.width - this.right,
                top = spaceSize.height - this.bottom,
                right = spaceSize.width - this.left,
                bottom = spaceSize.height - this.top,
            )
        }

        270 -> {
            Rect(
                left = this.top,
                top = spaceSize.width - this.right,
                right = this.bottom,
                bottom = spaceSize.width - this.left,
            )
        }

        else -> this
    }
}

@Stable
internal fun Rect.reverseRotateInSpace(spaceSize: Size, rotation: Int): Rect {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = 360 - rotation % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}


/* **************************************** IntRect ********************************************* */

@Stable
internal fun IntRect.toShortString(): String = "[${left}x${top},${right}x${bottom}]"

/**
 * Rounds a [Rect] to an [IntRect]
 */
@Stable
internal fun Rect.round(): IntRect = IntRect(
    left = left.roundToInt(),
    top = top.roundToInt(),
    right = right.roundToInt(),
    bottom = bottom.roundToInt()
)

@Stable
internal fun IntRect.scale(scale: Float): IntRect =
    IntRect(
        left = (left * scale).roundToInt(),
        top = (top * scale).roundToInt(),
        right = (right * scale).roundToInt(),
        bottom = (bottom * scale).roundToInt(),
    )

@Stable
internal fun IntRect.scale(scale: ScaleFactor): IntRect =
    IntRect(
        left = (left * scale.scaleX).roundToInt(),
        top = (top * scale.scaleY).roundToInt(),
        right = (right * scale.scaleX).roundToInt(),
        bottom = (bottom * scale.scaleY).roundToInt(),
    )

@Stable
internal fun IntRect.restoreScale(scale: Float): IntRect =
    IntRect(
        left = (left / scale).roundToInt(),
        top = (top / scale).roundToInt(),
        right = (right / scale).roundToInt(),
        bottom = (bottom / scale).roundToInt(),
    )

@Stable
internal fun IntRect.restoreScale(scaleFactor: ScaleFactor): IntRect =
    IntRect(
        left = (left / scaleFactor.scaleX).roundToInt(),
        top = (top / scaleFactor.scaleY).roundToInt(),
        right = (right / scaleFactor.scaleX).roundToInt(),
        bottom = (bottom / scaleFactor.scaleY).roundToInt(),
    )

@Stable
internal fun IntRect.limitTo(rect: IntRect): IntRect =
    if (this.left < rect.left
        || this.top < rect.top
        || this.right < rect.left || this.right > rect.right
        || this.bottom < rect.top || this.bottom > rect.bottom
    ) {
        IntRect(
            left = left.coerceAtLeast(rect.left),
            top = top.coerceAtLeast(rect.top),
            right = right.coerceIn(rect.left, rect.right),
            bottom = bottom.coerceIn(rect.top, rect.bottom),
        )
    } else {
        this
    }

@Stable
internal fun IntRect.limitTo(size: IntSize): IntRect =
    if (this.left < 0
        || this.top < 0
        || this.right < 0 || this.right > size.width
        || this.bottom < 0 || this.bottom > size.height
    ) {
        IntRect(
            left = left.coerceAtLeast(0),
            top = top.coerceAtLeast(0),
            right = right.coerceIn(0, size.width),
            bottom = bottom.coerceIn(0, size.height),
        )
    } else {
        this
    }

@Stable
internal fun IntRect.rotateInSpace(spaceSize: IntSize, rotation: Int): IntRect {
    require(rotation % 90 == 0) { "rotation must be a multiple of 90, rotation: $rotation" }
    return when (rotation % 360) {
        90 -> {
            IntRect(
                left = spaceSize.height - this.bottom,
                top = this.left,
                right = spaceSize.height - this.top,
                bottom = this.right
            )
        }

        180 -> {
            IntRect(
                left = spaceSize.width - this.right,
                top = spaceSize.height - this.bottom,
                right = spaceSize.width - this.left,
                bottom = spaceSize.height - this.top,
            )
        }

        270 -> {
            IntRect(
                left = this.top,
                top = spaceSize.width - this.right,
                right = this.bottom,
                bottom = spaceSize.width - this.left,
            )
        }

        else -> this
    }
}

@Stable
internal fun IntRect.reverseRotateInSpace(spaceSize: IntSize, rotation: Int): IntRect {
    val rotatedSpaceSize = spaceSize.rotate(rotation)
    val reverseRotation = 360 - rotation % 360
    return rotateInSpace(rotatedSpaceSize, reverseRotation)
}


/* ************************************** ScaleFactor ******************************************* */

@Stable
internal fun ScaleFactor.toShortString(): String = "${scaleX.format(2)}x${scaleY.format(2)}"

private val scaleFactorOrigin by lazy { ScaleFactor(scaleX = 1f, scaleY = 1f) }

@Stable
internal val ScaleFactor.Companion.Origin: ScaleFactor
    get() = scaleFactorOrigin

@Stable
internal operator fun ScaleFactor.times(scaleFactor: ScaleFactor) =
    ScaleFactor(scaleX * scaleFactor.scaleX, scaleY * scaleFactor.scaleY)

@Stable
internal operator fun ScaleFactor.div(scaleFactor: ScaleFactor) =
    ScaleFactor(scaleX / scaleFactor.scaleX, scaleY / scaleFactor.scaleY)

@Stable
internal fun ScaleFactor(scale: Float): ScaleFactor = ScaleFactor(scale, scale)


/* ************************************** TransformOrigin *************************************** */

@Stable
internal fun TransformOrigin.toShortString(): String =
    "${pivotFractionX.format(2)}x${pivotFractionY.format(2)}"

private val transformOriginTopStart by lazy { TransformOrigin(0f, 0f) }

@Stable
internal val TransformOrigin.Companion.TopStart: TransformOrigin
    get() = transformOriginTopStart

internal operator fun TransformOrigin.times(operand: Float) =
    TransformOrigin(pivotFractionX * operand, pivotFractionY * operand)

internal operator fun TransformOrigin.div(operand: Float) =
    TransformOrigin(pivotFractionX / operand, pivotFractionY / operand)

/**
 * Multiplication operator with [IntSize].
 *
 * Return a new [IntSize] with the width and height multiplied by the [TransformOrigin.pivotFractionX] and
 * [TransformOrigin.pivotFractionY] respectively
 */
internal operator fun IntSize.times(origin: TransformOrigin): IntSize =
    IntSize(
        width = (this.width * origin.pivotFractionX).roundToInt(),
        height = (this.height * origin.pivotFractionY).roundToInt()
    )

/**
 * Multiplication operator with [IntSize] with reverse parameter types to maintain
 * commutative properties of multiplication
 *
 * Return a new [IntSize] with the width and height multiplied by the [TransformOrigin.pivotFractionX] and
 * [TransformOrigin.pivotFractionY] respectively
 */
internal operator fun TransformOrigin.times(size: IntSize): IntSize = size * this

/**
 * Division operator with [IntSize]
 *
 * Return a new [IntSize] with the width and height divided by [TransformOrigin.pivotFractionX] and
 * [TransformOrigin.pivotFractionY] respectively
 */
internal operator fun IntSize.div(origin: TransformOrigin): IntSize =
    IntSize(
        width = (width / origin.pivotFractionX).roundToInt(),
        height = (height / origin.pivotFractionY).roundToInt()
    )

/**
 * Linearly interpolate between two [TransformOrigin] parameters
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid (and can
 * easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as
 * an `AnimationController`.
 */
internal fun lerp(
    start: TransformOrigin,
    stop: TransformOrigin,
    fraction: Float
): TransformOrigin {
    return TransformOrigin(
        pivotFractionX = androidx.compose.ui.util.lerp(
            start.pivotFractionX,
            stop.pivotFractionX,
            fraction
        ),
        pivotFractionY = androidx.compose.ui.util.lerp(
            start.pivotFractionY,
            stop.pivotFractionY,
            fraction
        )
    )
}


/* ************************************** ContentScale ****************************************** */

@Stable
internal val ContentScale.name: String
    get() = when (this) {
        ContentScale.FillWidth -> "FillWidth"
        ContentScale.FillHeight -> "FillHeight"
        ContentScale.FillBounds -> "FillBounds"
        ContentScale.Fit -> "Fit"
        ContentScale.Crop -> "Crop"
        ContentScale.Inside -> "Inside"
        ContentScale.None -> "None"
        else -> "Unknown ContentScale: $this"
    }

@Stable
internal fun ContentScale.Companion.valueOf(name: String): ContentScale {
    return when (name) {
        "FillWidth" -> FillWidth
        "FillHeight" -> FillHeight
        "FillBounds" -> FillBounds
        "Fit" -> Fit
        "Crop" -> Crop
        "Inside" -> Inside
        "None" -> None
        else -> throw IllegalArgumentException("Unknown ContentScale name: $name")
    }
}


/* ************************************** Alignment ********************************************* */

@Stable
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
        else -> "Unknown Alignment: $this"
    }

@Stable
internal fun Alignment.Companion.valueOf(name: String): Alignment {
    return when (name) {
        "TopStart" -> TopStart
        "TopCenter" -> TopCenter
        "TopEnd" -> TopEnd
        "CenterStart" -> CenterStart
        "Center" -> Center
        "CenterEnd" -> CenterEnd
        "BottomStart" -> BottomStart
        "BottomCenter" -> BottomCenter
        "BottomEnd" -> BottomEnd
        else -> throw IllegalArgumentException("Unknown alignment name: $name")
    }
}

@Stable
internal val Alignment.isStart: Boolean
    get() = this == Alignment.TopStart || this == Alignment.CenterStart || this == Alignment.BottomStart

@Stable
internal val Alignment.isHorizontalCenter: Boolean
    get() = this == Alignment.TopCenter || this == Alignment.Center || this == Alignment.BottomCenter

@Stable
internal val Alignment.isCenter: Boolean
    get() = this == Alignment.Center

@Stable
internal val Alignment.isEnd: Boolean
    get() = this == Alignment.TopEnd || this == Alignment.CenterEnd || this == Alignment.BottomEnd

@Stable
internal val Alignment.isTop: Boolean
    get() = this == Alignment.TopStart || this == Alignment.TopCenter || this == Alignment.TopEnd

@Stable
internal val Alignment.isVerticalCenter: Boolean
    get() = this == Alignment.CenterStart || this == Alignment.Center || this == Alignment.CenterEnd

@Stable
internal val Alignment.isBottom: Boolean
    get() = this == Alignment.BottomStart || this == Alignment.BottomCenter || this == Alignment.BottomEnd