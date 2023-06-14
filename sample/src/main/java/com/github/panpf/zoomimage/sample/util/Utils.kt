package com.github.panpf.zoomimage.sample.util

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.net.toUri
import com.github.panpf.zoomimage.Size
import java.io.File
import java.math.BigDecimal

internal fun sketchUri2CoilModel(context: Context, sketchImageUri: String): Any? {
    return when {
        sketchImageUri.startsWith("asset://") -> {
            sketchImageUri.replace("asset://", "file://filled/android_asset/").toUri()
        }

        sketchImageUri.startsWith("file://") -> {
            File(sketchImageUri.substring("file://".length))
        }

        sketchImageUri.startsWith("android.resource://") -> {
            val resId =
                sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()?.toIntOrNull()
            if (resId != null) {
                "android.resource://${context.packageName}/$resId".toUri()
            } else {
                Log.w("sketchUri2CoilModel", "Unsupported sketch resource uri: '$sketchImageUri'")
                null
            }
        }

        else -> {
            sketchImageUri.toUri()
        }
    }
}


internal fun Context.newCoilResourceUri(@DrawableRes @RawRes id: Int): Uri {
    return Uri.parse("android.resource://${packageName}/${id}")
}

internal fun newCoilAssetUri(@Suppress("SameParameterValue") path: String): Uri {
    return Uri.parse("file://filled/android_asset/$path")
}


internal fun Size.toShortString(): String = "(${width}x$height)"

internal fun Size.toVeryShortString(): String = "${width}x$height"

internal fun android.graphics.Rect.toVeryShortString(): String =
    "(${left},${top}-${right},${bottom})"

internal fun android.graphics.RectF.toVeryShortString(): String =
    "(${left.format(2)},${top.format(2)}-${right.format(2)},${bottom.format(2)})"

internal fun Rect.crossWith(other: Rect): Boolean {
    return this.left < other.right
            && this.right > other.left
            && this.top < other.bottom
            && this.bottom > other.top
}

internal fun Float.format(newScale: Int): Float =
    BigDecimal(toDouble()).setScale(newScale, BigDecimal.ROUND_HALF_UP).toFloat()

internal fun String.formatLength(targetLength: Int, padChar: Char = ' '): String {
    return if (this.length >= targetLength) {
        this.substring(0, targetLength)
    } else {
        this.padEnd(targetLength, padChar)
    }
}