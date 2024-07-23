package com.github.panpf.zoomimage.coil

import com.github.panpf.zoomimage.subsampling.ImageSource as ZoomImageImageSource
import android.content.Context
import coil.ImageLoader
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import okio.Path
import java.io.File
import java.net.URL

interface CoilModelToImageSource {
    fun dataToImageSource(model: Any): ZoomImageImageSource.Factory?
}

class CoilModelToImageSourceImpl constructor(
    private val context: Context,
    private val imageLoader: ImageLoader
) : CoilModelToImageSource {

    override fun dataToImageSource(model: Any): ZoomImageImageSource.Factory? {
        return when {
            model is URL -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is android.net.Uri && (model.scheme == "http" || model.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is String && model.startsWith("content://") -> {
                ImageSource.fromContent(context, android.net.Uri.parse(model)).toFactory()
            }

            model is android.net.Uri && model.scheme == "content" -> {
                ImageSource.fromContent(context, model).toFactory()
            }

            model is String && model.startsWith("file:///android_asset/") -> {
                val assetFileName = android.net.Uri.parse(model).pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is android.net.Uri && model.scheme == "file" && model.pathSegments.firstOrNull() == "android_asset" -> {
                val assetFileName = model.pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is Path -> {
                ZoomImageImageSource.fromFile(model).toFactory()
            }

            model is String && model.startsWith("file://") -> {
                val filePath = android.net.Uri.parse(model).path
                filePath?.let { ImageSource.fromFile(File(filePath)).toFactory() }
            }

            model is android.net.Uri && model.scheme == "file" -> {
                val filePath = model.path
                filePath?.let { ImageSource.fromFile(File(filePath)).toFactory() }
            }

            model is File -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is Int -> {
                ImageSource.fromResource(context, model).toFactory()
            }

            model is ByteArray -> {
                ImageSource.fromByteArray(model).toFactory()
            }

            else -> {
                null
            }
        }
    }
}