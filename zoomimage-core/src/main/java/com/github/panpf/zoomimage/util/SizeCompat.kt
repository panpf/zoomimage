package com.github.panpf.zoomimage.util

import com.github.panpf.zoomimage.util.internal.format
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

data class SizeCompat(val width: Float, val height: Float) {

    companion object {
        val Zero = SizeCompat(width = 0f, height = 0f)
    }

    fun isEmpty(): Boolean = width == 0f || height == 0f

    /**
     * Multiplication operator.
     *
     * Returns a [SizeCompat] whose dimensions are the dimensions of the left-hand-side
     * operand (a [SizeCompat]) multiplied by the scalar right-hand-side operand (a
     * [Float]).
     */
    operator fun times(operand: Float) = SizeCompat(width * operand, height * operand)

    /**
     * Division operator.
     *
     * Returns a [SizeCompat] whose dimensions are the dimensions of the left-hand-side
     * operand (a [SizeCompat]) divided by the scalar right-hand-side operand (a
     * [Float]).
     */
    operator fun div(operand: Float) = SizeCompat(width / operand, height / operand)

    /**
     * The lesser of the magnitudes of the [width] and the [height].
     */
    val minDimension: Float
        get() = min(width.absoluteValue, height.absoluteValue)

    /**
     * The greater of the magnitudes of the [width] and the [height].
     */
    val maxDimension: Float
        get() = max(width.absoluteValue, height.absoluteValue)

    override fun toString(): String =
        "SizeCompat(${width.format(2)}x${height.format(2)})"
}

/**
 * Returns a [SizeCompat] with [size]'s [SizeCompat.width] and [SizeCompat.height] multiplied by [this]
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun Int.times(size: SizeCompat) = size * this.toFloat()

/**
 * Returns a [SizeCompat] with [size]'s [SizeCompat.width] and [SizeCompat.height] multiplied by [this]
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun Double.times(size: SizeCompat) = size * this.toFloat()

/**
 * Convert a [SizeCompat] to a [RectCompat].
 */
fun SizeCompat.toRect(): RectCompat {
    return RectCompat(OffsetCompat.Zero, this)
}

/**
 * Returns a [SizeCompat] with [size]'s [SizeCompat.width] and [SizeCompat.height] multiplied by [this]
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun Float.times(size: SizeCompat) = size * this

/**
 * Returns the [OffsetCompat] of the center of the rect from the point of [0, 0]
 * with this [SizeCompat].
 */
val SizeCompat.center: OffsetCompat get() = OffsetCompat(x = width / 2f, y = height / 2f)


fun SizeCompat.toShortString(): String = "${width.format(2)}x${height.format(2)}"


val SizeCompat.isNotEmpty: Boolean
    get() = !isEmpty()

fun SizeCompat.isSameAspectRatio(other: SizeCompat, delta: Float = 0f): Boolean {
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

fun SizeCompat.rotate(rotation: Int): SizeCompat {
    return if (rotation % 180 == 0) this else SizeCompat(width = height, height = width)
}

operator fun SizeCompat.times(scaleFactor: ScaleFactorCompat): SizeCompat {
    return SizeCompat(width = width * scaleFactor.scaleX, height = height * scaleFactor.scaleY)
}

operator fun SizeCompat.div(scaleFactor: ScaleFactorCompat): SizeCompat {
    return SizeCompat(width = width / scaleFactor.scaleX, height = height / scaleFactor.scaleY)
}