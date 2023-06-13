package com.github.panpf.zoomimage.internal

import java.math.RoundingMode
import java.text.DecimalFormat

internal fun Float.format(
    decimalPlacesLength: Int = 2,
    decimalPlacesFillZero: Boolean = false,
    suffix: String? = null,
): String {
    val value = this
    val buffString = StringBuilder()
    buffString.append("#")
    if (decimalPlacesLength > 0) {
        buffString.append(".")
        for (w in 0 until decimalPlacesLength) {
            buffString.append(if (decimalPlacesFillZero) "0" else "#")
        }
    }
    val format = DecimalFormat(buffString.toString())
    format.roundingMode = RoundingMode.HALF_UP
    return if (suffix == null) {
        format.format(value)
    } else {
        format.format(value) + suffix
    }
}
