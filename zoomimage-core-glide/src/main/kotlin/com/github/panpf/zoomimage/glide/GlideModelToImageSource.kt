package com.github.panpf.zoomimage.glide

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import java.io.File
import java.net.URL

interface GlideModelToImageSource {
    fun dataToImageSource(model: Any): ImageSource?
}

class GlideModelToImageSourceImpl(private val context: Context) : GlideModelToImageSource {

    override fun dataToImageSource(model: Any): ImageSource? {
        return when {
            model is GlideUrl -> {
                GlideHttpImageSource(Glide.get(context), model)
            }

            model is URL -> {
                GlideHttpImageSource(Glide.get(context), GlideUrl(model))
            }

            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                GlideHttpImageSource(Glide.get(context), model)
            }

            model is Uri && (model.scheme == "http" || model.scheme == "https") -> {
                GlideHttpImageSource(Glide.get(context), model.toString())
            }

            model is String && model.startsWith("content://") -> {
                ImageSource.fromContent(context, Uri.parse(model))
            }

            model is Uri && model.scheme == "content" -> {
                ImageSource.fromContent(context, model)
            }

            model is String && model.startsWith("file:///android_asset/") -> {
                val assetFileName = Uri.parse(model).pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it) }
            }

            model is Uri && model.scheme == "file" && model.pathSegments.firstOrNull() == "android_asset" -> {
                val assetFileName = model.pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it) }
            }

            model is String && model.startsWith("file://") -> {
                val filePath = Uri.parse(model).path
                filePath?.let { ImageSource.fromFile(File(filePath)) }
            }

            model is Uri && model.scheme == "file" -> {
                val filePath = model.path
                filePath?.let { ImageSource.fromFile(File(filePath)) }
            }

            model is File -> {
                ImageSource.fromFile(model)
            }

            model is Int -> {
                ImageSource.fromResource(context, model)
            }

            model is ByteArray -> {
                ImageSource.fromByteArray(model)
            }

            else -> {
                null
            }
        }
    }
}