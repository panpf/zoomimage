package com.github.panpf.zoomimage

import kotlin.math.abs

open class Size(val width: Int, val height: Int) {

    constructor() : this(0, 0)

    val isEmpty: Boolean
        get() = width == 0 || height == 0

    operator fun component1(): Int = width

    operator fun component2(): Int = height

    override fun toString(): String = "Size(${width}, $height)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Size
        if (width != other.width) return false
        if (height != other.height) return false
        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        return result
    }

    companion object {

        val Empty = Size()

        @Throws(NumberFormatException::class)
        fun parseSize(string: String): Size {
            var sepIx = string.indexOf('*')
            if (sepIx < 0) {
                sepIx = string.indexOf('x')
            }
            if (sepIx < 0) {
                throw NumberFormatException("Invalid Size: \"$string\"")
            }
            return try {
                Size(string.substring(0, sepIx).toInt(), string.substring(sepIx + 1).toInt())
            } catch (e: NumberFormatException) {
                throw NumberFormatException("Invalid Size: \"$string\"")
            }
        }
    }
}

val Size.isNotEmpty: Boolean
    get() = !isEmpty

fun Size.isSameAspectRatio(other: Size, delta: Float = 0f): Boolean {
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

fun Size.rotate(rotateDegrees: Int): Size {
    return if (rotateDegrees % 180 == 0) this else Size(height, width)
}


fun Size.toShortString(): String = "(${width},$height)"