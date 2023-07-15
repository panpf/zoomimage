package com.github.panpf.zoomimage.core

import android.graphics.Rect
import com.github.panpf.zoomimage.core.internal.lerp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * An immutable, 2D, axis-aligned, floating-point rectangle whose coordinates
 * are relative to a given origin.
 */
data class IntRectCompat(
    /**
     * The offset of the left edge of this rectangle from the x axis.
     */
    val left: Int,

    /**
     * The offset of the top edge of this rectangle from the y axis.
     */
    val top: Int,

    /**
     * The offset of the right edge of this rectangle from the x axis.
     */
    val right: Int,

    /**
     * The offset of the bottom edge of this rectangle from the y axis.
     */
    val bottom: Int
) {
    companion object {

        /** A rectangle with left, top, right, and bottom edges all at zero. */
        val Zero: IntRectCompat = IntRectCompat(0, 0, 0, 0)
    }

    /** The distance between the left and right edges of this rectangle. */
    val width: Int
        get() {
            return right - left
        }

    /** The distance between the top and bottom edges of this rectangle. */
    val height: Int
        get() {
            return bottom - top
        }

    /**
     * The distance between the upper-left corner and the lower-right corner of
     * this rectangle.
     */
    val size: IntSizeCompat
        get() = IntSizeCompat(width, height)

    /**
     * Whether this rectangle encloses a non-zero area. Negative areas are
     * considered empty.
     */
    val isEmpty: Boolean
        get() = left >= right || top >= bottom

    /**
     * Returns a new rectangle translated by the given offset.
     *
     * To translate a rectangle by separate x and y components rather than by an
     * [OffsetCompat], consider [translate].
     */
    fun translate(offset: IntOffsetCompat): IntRectCompat {
        return IntRectCompat(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y)
    }

    /**
     * Returns a new rectangle with translateX added to the x components and
     * translateY added to the y components.
     */
    fun translate(translateX: Int, translateY: Int): IntRectCompat {
        return IntRectCompat(
            left + translateX,
            top + translateY,
            right + translateX,
            bottom + translateY
        )
    }

    /** Returns a new rectangle with edges moved outwards by the given delta. */
    fun inflate(delta: Int): IntRectCompat {
        return IntRectCompat(left - delta, top - delta, right + delta, bottom + delta)
    }

    /** Returns a new rectangle with edges moved inwards by the given delta. */
    fun deflate(delta: Int): IntRectCompat = inflate(-delta)

    /**
     * Returns a new rectangle that is the intersection of the given
     * rectangle and this rectangle. The two rectangles must overlap
     * for this to be meaningful. If the two rectangles do not overlap,
     * then the resulting IntRectCompat will have a negative width or height.
     */
    fun intersect(other: IntRectCompat): IntRectCompat {
        return IntRectCompat(
            kotlin.math.max(left, other.left),
            kotlin.math.max(top, other.top),
            kotlin.math.min(right, other.right),
            kotlin.math.min(bottom, other.bottom)
        )
    }

    /** Whether `other` has a nonzero area of overlap with this rectangle. */
    fun overlaps(other: IntRectCompat): Boolean {
        if (right <= other.left || other.right <= left)
            return false
        if (bottom <= other.top || other.bottom <= top)
            return false
        return true
    }

    /**
     * The lesser of the magnitudes of the [width] and the [height] of this
     * rectangle.
     */
    val minDimension: Int
        get() = kotlin.math.min(width.absoluteValue, height.absoluteValue)

    /**
     * The greater of the magnitudes of the [width] and the [height] of this
     * rectangle.
     */
    val maxDimension: Int
        get() = kotlin.math.max(width.absoluteValue, height.absoluteValue)

    /**
     * The offset to the intersection of the top and left edges of this rectangle.
     */
    val topLeft: IntOffsetCompat
        get() = IntOffsetCompat(left, top)

    /**
     * The offset to the center of the top edge of this rectangle.
     */
    val topCenter: IntOffsetCompat
        get() = IntOffsetCompat(left + width / 2, top)

    /**
     * The offset to the intersection of the top and right edges of this rectangle.
     */
    val topRight: IntOffsetCompat
        get() = IntOffsetCompat(right, top)

    /**
     * The offset to the center of the left edge of this rectangle.
     */
    val centerLeft: IntOffsetCompat
        get() = IntOffsetCompat(left, top + height / 2)

    /**
     * The offset to the point halfway between the left and right and the top and
     * bottom edges of this rectangle.
     *
     * See also [IntSizeCompat.center].
     */
    val center: IntOffsetCompat
        get() = IntOffsetCompat(left + width / 2, top + height / 2)

    /**
     * The offset to the center of the right edge of this rectangle.
     */
    val centerRight: IntOffsetCompat
        get() = IntOffsetCompat(right, top + height / 2)

    /**
     * The offset to the intersection of the bottom and left edges of this rectangle.
     */
    val bottomLeft: IntOffsetCompat
        get() = IntOffsetCompat(left, bottom)

    /**
     * The offset to the center of the bottom edge of this rectangle.
     */
    val bottomCenter: IntOffsetCompat
        get() {
            return IntOffsetCompat(left + width / 2, bottom)
        }

    /**
     * The offset to the intersection of the bottom and right edges of this rectangle.
     */
    val bottomRight: IntOffsetCompat
        get() {
            return IntOffsetCompat(right, bottom)
        }

    /**
     * Whether the point specified by the given offset (which is assumed to be
     * relative to the origin) lies between the left and right and the top and
     * bottom edges of this rectangle.
     *
     * Rectangles include their top and left edges but exclude their bottom and
     * right edges.
     */
    fun contains(offset: IntOffsetCompat): Boolean {
        return offset.x >= left && offset.x < right && offset.y >= top && offset.y < bottom
    }

    override fun toString() = "IntRectCompat.fromLTRB(" +
            "$left, " +
            "$top, " +
            "$right, " +
            "$bottom)"
}

/**
 * Construct a rectangle from its left and top edges as well as its width and height.
 * @param offset OffsetCompat to represent the top and left parameters of the RectCompat
 * @param size Size to determine the width and height of this [IntRectCompat].
 * @return RectCompat with [IntRectCompat.left] and [IntRectCompat.top] configured to [IntOffsetCompat.x] and
 * [IntOffsetCompat.y] as [IntRectCompat.right] and [IntRectCompat.bottom] to [IntOffsetCompat.x] + [IntSizeCompat.width] and
 * [IntOffsetCompat.y] + [IntSizeCompat.height] respectively
 */
fun IntRectCompat(offset: IntOffsetCompat, size: IntSizeCompat) =
    IntRectCompat(
        left = offset.x,
        top = offset.y,
        right = offset.x + size.width,
        bottom = offset.y + size.height
    )

/**
 * Construct the smallest rectangle that encloses the given offsets, treating
 * them as vectors from the origin.
 * @param topLeft OffsetCompat representing the left and top edges of the rectangle
 * @param bottomRight OffsetCompat representing the bottom and right edges of the rectangle
 */
fun IntRectCompat(topLeft: IntOffsetCompat, bottomRight: IntOffsetCompat): IntRectCompat =
    IntRectCompat(
        topLeft.x,
        topLeft.y,
        bottomRight.x,
        bottomRight.y
    )

/**
 * Construct a rectangle that bounds the given circle
 * @param center OffsetCompat that represents the center of the circle
 * @param radius Radius of the circle to enclose
 */
fun IntRectCompat(center: IntOffsetCompat, radius: Int): IntRectCompat =
    IntRectCompat(
        center.x - radius,
        center.y - radius,
        center.x + radius,
        center.y + radius
    )

/**
 * Linearly interpolate between two rectangles.
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
fun lerp(start: IntRectCompat, stop: IntRectCompat, fraction: Float): IntRectCompat {
    return IntRectCompat(
        lerp(start.left, stop.left, fraction),
        lerp(start.top, stop.top, fraction),
        lerp(start.right, stop.right, fraction),
        lerp(start.bottom, stop.bottom, fraction)
    )
}


fun IntRectCompat.toShortString(): String = "${left}x${top},${right}x${bottom}"

/**
 * Converts an [IntRectCompat] to a [RectCompat]
 */
fun IntRectCompat.toCompatRect(): RectCompat = RectCompat(
    left = left.toFloat(),
    top = top.toFloat(),
    right = right.toFloat(),
    bottom = bottom.toFloat()
)

/**
 * Rounds a [RectCompat] to an [IntRectCompat]
 */
fun RectCompat.roundToCompatIntRect(): IntRectCompat = IntRectCompat(
    left = left.roundToInt(),
    top = top.roundToInt(),
    right = right.roundToInt(),
    bottom = bottom.roundToInt()
)