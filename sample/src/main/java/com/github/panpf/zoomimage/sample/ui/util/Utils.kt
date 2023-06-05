package com.github.panpf.zoomimage.sample.ui.util

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import com.github.panpf.zoomimage.format


fun Context.newCoilResourceUri(@DrawableRes id: Int): Uri {
    return Uri.parse("android.resource://${packageName}/${id}")
}

fun newCoilAssetUri(@Suppress("SameParameterValue") path: String): Uri {
    return Uri.parse("file://filled/android_asset/$path")
}

fun com.github.panpf.zoomimage.Size.toShortString(): String = "(${width}x$height)"


fun android.graphics.Rect.toVeryShortString(): String =
    "(${left},${top} - ${right},${bottom})"

fun android.graphics.RectF.toVeryShortString(): String =
    "(${left.format(2)},${top.format(2)} - ${right.format(2)},${bottom.format(2)})"