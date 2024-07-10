package com.github.panpf.zoomimage.sample.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.net.toUri
import java.io.File


actual fun sketchUri2CoilModel(context: Context, sketchImageUri: String): Any? {
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

fun sketchUri2GlideModel(sketchImageUri: String): Any {
    return when {
        sketchImageUri.startsWith("asset://") ->
            sketchImageUri.replace("asset://", "file:///android_asset/")

        sketchImageUri.startsWith("android.resource://") -> {
            sketchImageUri.toUri().getQueryParameters("resId").firstOrNull()?.toIntOrNull()
                ?: throw IllegalArgumentException("Can't use Subsampling, invalid resource uri: '$sketchImageUri'")
        }

        sketchImageUri.startsWith("compose.resource://") -> {
            val resourceName = sketchImageUri.toUri().lastPathSegment
            "file:///android_asset/$resourceName"
        }

        else -> sketchImageUri
    }
}


internal fun Context.newCoilResourceUri(@DrawableRes @RawRes id: Int): Uri {
    return Uri.parse("android.resource://${packageName}/${id}")
}

internal fun newCoilAssetUri(@Suppress("SameParameterValue") path: String): Uri {
    return Uri.parse("file://filled/android_asset/$path")
}