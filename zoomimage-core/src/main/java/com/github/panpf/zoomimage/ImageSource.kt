package com.github.panpf.zoomimage

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.WorkerThread
import java.io.File
import java.io.InputStream

interface ImageSource {

    val key: String

    @WorkerThread
    suspend fun openInputStream(): Result<InputStream>

    companion object {
        fun fromUri(context: Context, uri: Uri): ImageSource {
            return ContentImageSource(context, uri)
        }

        fun fromResource(resources: Resources, @RawRes @DrawableRes drawableId: Int): ImageSource {
            return ResourceImageSource(resources, drawableId)
        }

        fun fromResource(context: Context, @RawRes @DrawableRes drawableId: Int): ImageSource {
            return ResourceImageSource(context, drawableId)
        }

        fun fromAsset(context: Context, assetFileName: String): ImageSource {
            return AssetImageSource(context, assetFileName)
        }

        fun fromFile(file: File): ImageSource {
            return FileImageSource(file)
        }
    }
}