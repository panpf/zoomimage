package com.github.panpf.zoomimage.coil.internal

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okio.Buffer
import okio.Path.Companion.toPath
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.File
import java.nio.ByteBuffer

/**
 * @see com.github.panpf.zoomimage.core.coil2.test.CoreCoilUtilsTest.testDataToImageSource
 */
suspend fun dataToImageSource(
    context: Context,
    imageLoader: ImageLoader,
    request: ImageRequest,
): ImageSource.Factory? {
    val data = request.data
    val uri = when (data) {
        is String -> android.net.Uri.parse(data)
        is android.net.Uri -> data
        else -> null
    }
    return when {
        data is HttpUrl && (data.scheme == "http" || data.scheme == "https") -> {
            CoilHttpImageSource.Factory(context, imageLoader, request)
        }

        uri != null && (uri.scheme == "http" || uri.scheme == "https") -> {
            CoilHttpImageSource.Factory(context, imageLoader, request)
        }

        uri != null && uri.scheme == "content" -> {
            val androidUri = android.net.Uri.parse(data.toString())
            ImageSource.fromContent(context, androidUri).toFactory()
        }

        // file:///android_asset/image.jpg
        uri != null && uri.scheme == "file" && uri.pathSegments.firstOrNull() == "android_asset" -> {
            val assetFileName = uri.pathSegments.drop(1).joinToString("/")
            ImageSource.fromAsset(context, assetFileName).toFactory()
        }

        // /sdcard/xxx.jpg
        uri != null && uri.scheme?.takeIf { it.isNotEmpty() } == null
                && uri.authority?.takeIf { it.isNotEmpty() } == null
                && uri.path?.startsWith("/") == true -> {
            ImageSource.fromFile(uri.path!!.toPath()).toFactory()
        }

        // file:///sdcard/xxx.jpg
        uri != null && uri.scheme == "file"
                && uri.authority?.takeIf { it.isNotEmpty() } == null
                && uri.path?.startsWith("/") == true -> {
            ImageSource.fromFile(uri.path!!.toPath()).toFactory()
        }

        data is File -> {
            ImageSource.fromFile(data).toFactory()
        }

        data is Int -> {
            ImageSource.fromResource(context, data).toFactory()
        }

        // android.resource://example.package.name/drawable/image
        uri != null && uri.scheme == "android.resource" && uri.pathSegments.size == 2 -> {
            val packageName = uri.authority?.takeIf { it.isNotEmpty() } ?: context.packageName
            val resources = context.packageManager.getResourcesForApplication(packageName)
            val (type, name) = uri.pathSegments
            //noinspection DiscouragedApi: Necessary to support resource URIs.
            val id = resources.getIdentifier(name, type, packageName)
            ImageSource.fromResource(resources, id).toFactory()
        }

        // android.resource://example.package.name/4125123
        uri != null && uri.scheme == "android.resource" && uri.pathSegments.size == 1 -> {
            val packageName = uri.authority?.takeIf { it.isNotEmpty() } ?: context.packageName
            val resources = context.packageManager.getResourcesForApplication(packageName)
            val id = uri.pathSegments.first().toInt()
            ImageSource.fromResource(resources, id).toFactory()
        }

        data is ByteArray -> {
            ImageSource.fromByteArray(data).toFactory()
        }

        data is ByteBuffer -> {
            val byteArray: ByteArray = withContext(Dispatchers.IO) {
                data.asSource().buffer().use { it.readByteArray() }
            }
            ImageSource.fromByteArray(byteArray).toFactory()
        }

        else -> {
            null
        }
    }
}

/**
 * @see com.github.panpf.zoomimage.core.coil3.android.test.internal.CoilCoreUtilsAndroidTest.testDataToImageSource
 */
@Deprecated("Please use dataToImageSource(context, imageLoader, request) instead")
suspend fun dataToImageSource(
    context: Context,
    imageLoader: ImageLoader,
    model: Any
): ImageSource.Factory? = dataToImageSource(
    context = context,
    imageLoader = imageLoader,
    request = ImageRequest.Builder(context)
        .data(model)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()
)

internal fun ByteBuffer.asSource() = object : Source {
    private val buffer = this@asSource.slice()
    private val len = buffer.capacity()

    override fun close() = Unit

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (buffer.position() == len) return -1
        val pos = buffer.position()
        val newLimit = (pos + byteCount).toInt().coerceAtMost(len)
        buffer.limit(newLimit)
        return sink.write(buffer).toLong()
    }

    override fun timeout() = Timeout.NONE
}