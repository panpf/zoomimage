package com.github.panpf.zoomimage.internal

import java.math.RoundingMode
import java.text.DecimalFormat

internal fun Float.formatString(
    scale: Int = 2,
    fillZero: Boolean = false,
    suffix: String? = null,
): String {
    val value = this
    val patternBuilder = StringBuilder().apply {
        append("#")
        if (scale > 0) {
            append(".")
            for (w in 0 until scale) {
                append(if (fillZero) "0" else "#")
            }
        }
    }
    val format = DecimalFormat(patternBuilder.toString()).apply {
        roundingMode = RoundingMode.HALF_UP
    }
    return if (suffix == null) {
        format.format(value)
    } else {
        format.format(value) + suffix
    }
}
