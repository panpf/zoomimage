package com.github.panpf.zoomimage.util

import kotlin.math.abs
import kotlin.math.roundToInt

// todo Unit tests
data class IntSizeCompat(val width: Int, val height: Int) {

    /**
     * Returns an IntSize scaled by multiplying [width] and [height] by [other]
     */
    operator fun times(other: Int): IntSizeCompat =
        IntSizeCompat(width = width * other, height = height * other)

    /**
     * Returns an IntSize scaled by dividing [width] and [height] by [other]
     */
    operator fun div(other: Int): IntSizeCompat =
        IntSizeCompat(width = width / other, height = height / other)

    override fun toString(): String = "IntSizeCompat(${width}x$height)"

    companion object {
        val Zero = IntSizeCompat(width = 0, height = 0)
    }
}

/**
 * Returns an [IntSizeCompat] with [size]'s [IntSizeCompat.width] and [IntSizeCompat.height]
 * multiplied by [this].
 */
operator fun Int.times(size: IntSizeCompat) = size * this

/**
 * Convert a [IntSizeCompat] to a [IntRectCompat].
 */
fun IntSizeCompat.toIntRect(): IntRectCompat {
    return IntRectCompat(IntOffsetCompat.Zero, this)
}

/**
 * Returns the [IntOffsetCompat] of the center of the rect from the point of [0, 0]
 * with this [IntSizeCompat].
 */
val IntSizeCompat.center: IntOffsetCompat
    get() = IntOffsetCompat(x = width / 2, y = height / 2)


fun IntSizeCompat.toShortString(): String = "${width}x$height"

fun IntSizeCompat.isEmpty(): Boolean = width <= 0 || height <= 0

fun IntSizeCompat.isNotEmpty(): Boolean = width > 0 && height > 0

fun IntSizeCompat.isSameAspectRatio(other: IntSizeCompat, delta: Float = 0f): Boolean {
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

fun IntSizeCompat.rotate(rotation: Int): IntSizeCompat {
    return if (rotation % 180 == 0) this else IntSizeCompat(width = height, height = width)
}

fun IntSizeCompat.toSize(): SizeCompat = SizeCompat(width.toFloat(), height.toFloat())

fun SizeCompat.round(): IntSizeCompat =
    IntSizeCompat(width.roundToInt(), height.roundToInt())

operator fun IntSizeCompat.times(scaleFactor: ScaleFactorCompat): IntSizeCompat {
    return IntSizeCompat(
        width = (width * scaleFactor.scaleX).roundToInt(),
        height = (height * scaleFactor.scaleY).roundToInt()
    )
}

operator fun IntSizeCompat.div(scaleFactor: ScaleFactorCompat): IntSizeCompat {
    return IntSizeCompat(
        width = (width / scaleFactor.scaleX).roundToInt(),
        height = (height / scaleFactor.scaleY).roundToInt()
    )
}