package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.SizeCompat
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

enum class RoundMode {
    CEIL,
    FLOOR,
    ROUND,
}

fun SizeCompat.roundToIntWithMode(roundMode: RoundMode): IntSizeCompat = when (roundMode) {
    RoundMode.CEIL -> IntSizeCompat(width = ceil(width).toInt(), ceil(height).toInt())
    RoundMode.FLOOR -> IntSizeCompat(width = floor(width).toInt(), floor(height).toInt())
    else -> IntSizeCompat(width = width.roundToInt(), height.roundToInt())
}