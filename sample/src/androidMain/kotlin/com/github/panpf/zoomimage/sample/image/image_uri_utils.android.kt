package com.github.panpf.zoomimage.sample.image

import android.content.Context
import coil3.PlatformContext
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri

actual fun sketchUri2CoilModel(context: PlatformContext, sketchImageUri: String): Any? {
    return when {
        sketchImageUri.startsWith("android.resource:///") -> {
            sketchImageUri.replace(
                oldValue = "android.resource:///",
                newValue = "android.resource://${context.packageName}/"
            )
        }

        sketchImageUri.startsWith("/") -> {
            "file://$sketchImageUri"
        }

        else -> {
            sketchImageUri
        }
    }
}

fun sketchUri2GlideModel(context: Context, sketchImageUri: String): Any {
    val uri = sketchImageUri.toUri()
    return when {
        sketchImageUri.startsWith("android.resource:///") -> {
            sketchImageUri.replace(
                oldValue = "android.resource:///",
                newValue = "android.resource://${context.packageName}/"
            )
        }

        sketchImageUri.startsWith("/") -> {
            "file://$sketchImageUri"
        }

        isComposeResourceUri(uri) -> {
            val resourceName = uri.pathSegments.lastOrNull()
            "file:///android_asset/$resourceName"
        }

        else -> {
            sketchImageUri
        }
    }
}

fun sketchUri2PicassoData(context: Context, sketchImageUri: String): String {
    return when {
        sketchImageUri.startsWith("android.resource:///") -> {
            sketchImageUri.replace(
                oldValue = "android.resource:///",
                newValue = "android.resource://${context.packageName}/"
            )
        }

        sketchImageUri.startsWith("/") -> {
            "file://$sketchImageUri"
        }

        else -> {
            sketchImageUri
        }
    }
}