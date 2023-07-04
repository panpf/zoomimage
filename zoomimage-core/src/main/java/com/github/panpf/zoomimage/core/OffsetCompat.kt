package com.github.panpf.zoomimage.core

import com.github.panpf.zoomimage.core.internal.format

data class OffsetCompat(
    val x: Float,
    val y: Float
) {

    operator fun times(operand: Float) = OffsetCompat(x * operand, y * operand)

    operator fun div(operand: Float) = OffsetCompat(x / operand, y / operand)

    /**
     * Binary subtraction operator.
     *
     * Returns an offset whose [x] value is the left-hand-side operand's [x]
     * minus the right-hand-side operand's [x] and whose [y] value is the
     * left-hand-side operand's [y] minus the right-hand-side operand's [y].
     */
    operator fun minus(other: OffsetCompat): OffsetCompat = OffsetCompat(x - other.x, y - other.y)

    /**
     * Binary addition operator.
     *
     * Returns an offset whose [x] value is the sum of the [x] values of the
     * two operands, and whose [y] value is the sum of the [y] values of the
     * two operands.
     */
    operator fun plus(other: OffsetCompat): OffsetCompat = OffsetCompat(x + other.x, y + other.y)

    override fun toString() = "Offset(${x.roundToTenths()}, ${y.roundToTenths()})"

    companion object {
        val Unspecified = OffsetCompat(x = Float.NaN, y = Float.NaN)
        val Zero = OffsetCompat(x = 0f, y = 0f)
    }
}

private fun Float.roundToTenths(): Float {
    val shifted = this * 10
    val decimal = shifted - shifted.toInt()
    // Kotlin's round operator rounds 0.5f down to 0. Manually compare against
    // 0.5f and round up if necessary
    val roundedShifted = if (decimal >= 0.5f) {
        shifted.toInt() + 1
    } else {
        shifted.toInt()
    }
    return roundedShifted.toFloat() / 10
}

/**
 * `false` when this is [OffsetCompat.Unspecified].
 */
inline val OffsetCompat.isSpecified: Boolean
    get() = !x.isNaN() && !y.isNaN()

/**
 * `true` when this is [OffsetCompat.Unspecified].
 */
inline val OffsetCompat.isUnspecified: Boolean
    get() = x.isNaN() || y.isNaN()

/**
 * If this [OffsetCompat] [isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun OffsetCompat.takeOrElse(block: () -> OffsetCompat): OffsetCompat =
    if (isSpecified) this else block()

/**
 * Linearly interpolate between two [OffsetCompat] parameters
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
fun lerp(start: OffsetCompat, stop: OffsetCompat, fraction: Float): OffsetCompat {
    return OffsetCompat(
        lerp(start.x, stop.x, fraction),
        lerp(start.y, stop.y, fraction)
    )
}

/**
 * Linearly interpolate between [start] and [stop] with [fraction] fraction between them.
 */
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

fun OffsetCompat.toShortString(): String =
    "(${x.format(1)},${y.format(1)})"