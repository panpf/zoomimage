package com.github.panpf.zoomimage.sample.util

import android.content.Context
import androidx.core.content.PermissionChecker

fun android.graphics.PointF.toShortString(): String =
    "${x.format(2)}x${y.format(2)}"

fun android.graphics.Rect.toVeryShortString(): String =
    "[${left}x${top},${right}x${bottom}]"

fun android.graphics.RectF.toVeryShortString(): String =
    "[${left.format(2)}x${top.format(2)},${right.format(2)}x${bottom.format(2)}]"

fun checkPermissionGranted(context: Context, permission: String): Boolean {
    val result = PermissionChecker.checkSelfPermission(context, permission)
    return result == PermissionChecker.PERMISSION_GRANTED
}