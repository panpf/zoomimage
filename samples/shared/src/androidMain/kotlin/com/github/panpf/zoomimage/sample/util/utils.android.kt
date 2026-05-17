package com.github.panpf.zoomimage.sample.util

fun android.graphics.PointF.toShortString(): String =
    "${x.format(2)}x${y.format(2)}"

fun android.graphics.Rect.toVeryShortString(): String =
    "[${left}x${top},${right}x${bottom}]"

fun android.graphics.RectF.toVeryShortString(): String =
    "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"