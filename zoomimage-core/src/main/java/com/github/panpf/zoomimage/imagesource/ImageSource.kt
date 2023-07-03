package com.github.panpf.zoomimage.imagesource

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
        fun fromContent(context: Context, uri: Uri): ImageSource {
            return ContentImageSource(context, uri)
        }

        fun fromResource(resources: Resources, @RawRes @DrawableRes resId: Int): ImageSource {
            return ResourceImageSource(resources, resId)
        }

        fun fromResource(context: Context, @RawRes @DrawableRes resId: Int): ImageSource {
            return ResourceImageSource(context, resId)
        }

        fun fromAsset(context: Context, assetFileName: String): ImageSource {
            return AssetImageSource(context, assetFileName)
        }

        fun fromFile(file: File): ImageSource {
            return FileImageSource(file)
        }
    }
}