package com.github.panpf.zoomimage.sample.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.net.toUri
import com.github.panpf.zoomimage.core.Size
import com.github.panpf.zoomimage.format
import java.io.File

fun String.formatLength(targetLength: Int, padChar: Char = ' '): String {
    return if (this.length >= targetLength) {
        this.substring(0, targetLength)
    } else {
        this.padEnd(targetLength, padChar)
    }
}

fun sketchUri2CoilModel(context: Context, sketchImageUri: String): Any? {
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


fun Context.newCoilResourceUri(@DrawableRes @RawRes id: Int): Uri {
    return Uri.parse("android.resource://${packageName}/${id}")
}

fun newCoilAssetUri(@Suppress("SameParameterValue") path: String): Uri {
    return Uri.parse("file://filled/android_asset/$path")
}

fun Size.toShortString(): String = "(${width}x$height)"

fun Size.toVeryShortString(): String = "${width}x$height"


fun android.graphics.Rect.toVeryShortString(): String =
    "(${left},${top}-${right},${bottom})"

fun android.graphics.RectF.toVeryShortString(): String =
    "(${left.format(2)},${top.format(2)}-${right.format(2)},${bottom.format(2)})"