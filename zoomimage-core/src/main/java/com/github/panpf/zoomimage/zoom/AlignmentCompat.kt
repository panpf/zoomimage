package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.math.roundToInt

/**
 * An interface to calculate the position of a sized box inside an available space. [AlignmentCompat] is
 * often used to define the alignment of a layout inside a parent layout.
 */
fun interface AlignmentCompat {
    /**
     * Calculates the position of a box of size [size] relative to the top left corner of an area
     * of size [space]. The returned offset can be negative or larger than `space - size`,
     * meaning that the box will be positioned partially or completely outside the area.
     */
    fun align(size: IntSizeCompat, space: IntSizeCompat, ltrLayout: Boolean): IntOffsetCompat

    /**
     * A collection of common [AlignmentCompat]s aware of layout direction.
     */
    companion object {
        val TopStart: AlignmentCompat = BiasAlignmentCompat(-1f, -1f)
        val TopCenter: AlignmentCompat = BiasAlignmentCompat(0f, -1f)
        val TopEnd: AlignmentCompat = BiasAlignmentCompat(1f, -1f)
        val CenterStart: AlignmentCompat = BiasAlignmentCompat(-1f, 0f)
        val Center: AlignmentCompat = BiasAlignmentCompat(0f, 0f)
        val CenterEnd: AlignmentCompat = BiasAlignmentCompat(1f, 0f)
        val BottomStart: AlignmentCompat = BiasAlignmentCompat(-1f, 1f)
        val BottomCenter: AlignmentCompat = BiasAlignmentCompat(0f, 1f)
        val BottomEnd: AlignmentCompat = BiasAlignmentCompat(1f, 1f)
    }
}

data class BiasAlignmentCompat(
    val horizontalBias: Float,
    val verticalBias: Float
) : AlignmentCompat {
    override fun align(
        size: IntSizeCompat,
        space: IntSizeCompat,
        ltrLayout: Boolean
    ): IntOffsetCompat {
        // Convert to Px first and only round at the end, to avoid rounding twice while calculating
        // the new positions
        val centerX = (space.width - size.width).toFloat() / 2f
        val centerY = (space.height - size.height).toFloat() / 2f
        val resolvedHorizontalBias = if (ltrLayout) {
            horizontalBias
        } else {
            -1 * horizontalBias
        }

        val x = centerX * (1 + resolvedHorizontalBias)
        val y = centerY * (1 + verticalBias)
        return IntOffsetCompat(x.roundToInt(), y.roundToInt())
    }
}

val AlignmentCompat.name: String
    get() = when (this) {
        AlignmentCompat.TopStart -> "TopStart"
        AlignmentCompat.TopCenter -> "TopCenter"
        AlignmentCompat.TopEnd -> "TopEnd"
        AlignmentCompat.CenterStart -> "CenterStart"
        AlignmentCompat.Center -> "Center"
        AlignmentCompat.CenterEnd -> "CenterEnd"
        AlignmentCompat.BottomStart -> "BottomStart"
        AlignmentCompat.BottomCenter -> "BottomCenter"
        AlignmentCompat.BottomEnd -> "BottomEnd"
        else -> "Unknown AlignmentCompat: $this"
    }

fun AlignmentCompat.Companion.valueOf(name: String): AlignmentCompat {
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

val AlignmentCompat.isStart: Boolean
    get() = this == AlignmentCompat.TopStart || this == AlignmentCompat.CenterStart || this == AlignmentCompat.BottomStart
val AlignmentCompat.isHorizontalCenter: Boolean
    get() = this == AlignmentCompat.TopCenter || this == AlignmentCompat.Center || this == AlignmentCompat.BottomCenter
val AlignmentCompat.isCenter: Boolean
    get() = this == AlignmentCompat.Center
val AlignmentCompat.isEnd: Boolean
    get() = this == AlignmentCompat.TopEnd || this == AlignmentCompat.CenterEnd || this == AlignmentCompat.BottomEnd
val AlignmentCompat.isTop: Boolean
    get() = this == AlignmentCompat.TopStart || this == AlignmentCompat.TopCenter || this == AlignmentCompat.TopEnd
val AlignmentCompat.isVerticalCenter: Boolean
    get() = this == AlignmentCompat.CenterStart || this == AlignmentCompat.Center || this == AlignmentCompat.CenterEnd
val AlignmentCompat.isBottom: Boolean
    get() = this == AlignmentCompat.BottomStart || this == AlignmentCompat.BottomCenter || this == AlignmentCompat.BottomEnd