package com.github.panpf.zoomimage.picasso

import android.content.Context
import android.net.Uri
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import com.squareup.picasso.Picasso
import okio.Path.Companion.toOkioPath
import java.io.File

interface PicassoDataToImageSource {
    fun dataToImageSource(data: Any): ImageSource.Factory?
}

class PicassoDataToImageSourceImpl(private val context: Context) : PicassoDataToImageSource {

    override fun dataToImageSource(data: Any): ImageSource.Factory? {
        if (data is Uri) {
            return when {
                data.scheme == "http" || data.scheme == "https" -> {
                    PicassoHttpImageSource(Picasso.get(), data).toFactory()
                }

                data.scheme == "content" -> {
                    ImageSource.fromContent(context, data).toFactory()
                }

                data.scheme == "file" && data.pathSegments.firstOrNull() == "android_asset" -> {
                    val assetFileName = data.pathSegments
                        .takeIf { it.size > 1 }
                        ?.let { it.subList(1, it.size) }
                        ?.joinToString(separator = "/")
                    assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
                }

                data.scheme == "file" -> {
                    val filePath = data.path
                    filePath?.let { ImageSource.fromFile(File(filePath).toOkioPath()).toFactory() }
                }

                else -> {
                    null
                }
            }
        }

        if (data is Int && data != 0) {
            return ImageSource.fromResource(context, data).toFactory()
        }

        return null
    }
}