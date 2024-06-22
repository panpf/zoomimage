package com.github.panpf.zoomimage.sample.util

internal fun android.graphics.PointF.toShortString(): String =
    "${x.format(2)}x${y.format(2)}"

internal fun android.graphics.Rect.toVeryShortString(): String =
    "[${left}x${top},${right}x${bottom}]"

internal fun android.graphics.RectF.toVeryShortString(): String =
    "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"