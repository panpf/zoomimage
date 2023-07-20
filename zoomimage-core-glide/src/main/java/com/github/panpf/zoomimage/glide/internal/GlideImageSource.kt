package com.github.panpf.zoomimage.glide.internal

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.subsampling.ImageSource
import java.io.File


fun newGlideImageSource(context: Context, model: Any?): ImageSource? {
    return when {
        model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
            GlideHttpImageSource(Glide.get(context), model)
        }

        model is String && model.startsWith("content://") -> {
            ImageSource.fromContent(context, Uri.parse(model))
        }

        model is String && model.startsWith("file:///android_asset/") -> {
            val assetFileName = model.replace("file:///android_asset/", "")
            ImageSource.fromAsset(context, assetFileName)
        }

        model is String && model.startsWith("file://") -> {
            ImageSource.fromFile(File(model.replace("file://", "")))
        }

        model is Int -> {
            ImageSource.fromResource(context, model)
        }

        else -> {
            null
        }
    }
}