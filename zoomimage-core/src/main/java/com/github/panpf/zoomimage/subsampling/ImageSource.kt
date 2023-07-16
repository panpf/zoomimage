package com.github.panpf.zoomimage.subsampling

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
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

class AssetImageSource(val context: Context, val assetFileName: String) : ImageSource {

    override val key: String = "asset://$assetFileName"

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                context.assets.open(assetFileName)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AssetImageSource
        if (context != other.context) return false
        if (assetFileName != other.assetFileName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + assetFileName.hashCode()
        return result
    }

    override fun toString(): String {
        return "AssetImageSource('$assetFileName')"
    }
}

class ContentImageSource(val context: Context, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                context.contentResolver.openInputStream(uri)
                    ?: throw FileNotFoundException("Unable to open stream. uri='$uri'")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ContentImageSource
        if (context != other.context) return false
        if (uri != other.uri) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

    override fun toString(): String {
        return "ContentImageSource('$uri')"
    }
}

class FileImageSource(val file: File) : ImageSource {

    override val key: String = file.path

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                FileInputStream(file)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FileImageSource
        if (file != other.file) return false
        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }

    override fun toString(): String {
        return "FileImageSource('$file')"
    }
}

class ResourceImageSource(
    val resources: Resources,
    @RawRes @DrawableRes val resId: Int
) : ImageSource {

    constructor(
        context: Context,
        @RawRes @DrawableRes drawableId: Int
    ) : this(context.resources, drawableId)

    override val key: String = "android.resources://resource?resId=$resId"

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                resources.openRawResource(resId)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ResourceImageSource
        if (resources != other.resources) return false
        if (resId != other.resId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = resources.hashCode()
        result = 31 * result + resId
        return result
    }

    override fun toString(): String {
        return "ResourceImageSource(resId=$resId)"
    }
}