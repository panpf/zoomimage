package com.github.panpf.zoomimage.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.Uri
import coil3.toUri
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.toFactory
import okio.Path

actual class CoilModelToImageSourceImpl actual constructor(
    private val context: PlatformContext,
    private val imageLoader: ImageLoader
) : CoilModelToImageSource {

    actual override fun dataToImageSource(model: Any): ImageSource.Factory? {
        return when {
            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is Uri && (model.scheme == "http" || model.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is String && model.startsWith("/") -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is String && model.startsWith("file://") -> {
                val filePath = model.toUri().path
                filePath?.let { ImageSource.fromFile(filePath).toFactory() }
            }

            model is Uri && model.scheme == "file" -> {
                val filePath = model.path
                filePath?.let { ImageSource.fromFile(filePath).toFactory() }
            }

            model is Path -> {
                ImageSource.fromFile(model).toFactory()
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