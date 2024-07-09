package com.github.panpf.zoomimage.sample.util

import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.util.MimeTypeMap
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import platform.Foundation.NSBundle

class CoilKotlinResourceUriFetcher(
    val fileSystem: FileSystem,
    val resourceName: String,
) : Fetcher {

    companion object {
        const val SCHEME = "kotlin.resource"
    }

    override suspend fun fetch(): FetchResult {
        val resourcePath = NSBundle.mainBundle.resourcePath!!.toPath()
        val filePath = resourcePath.resolve("compose-resources").resolve(resourceName)
        val source = fileSystem.source(filePath)
        val imageSource = ImageSource(
            source = source.buffer(),
            fileSystem = fileSystem,
            metadata = null
        )
        val mimeType = getMimeType(resourceName, null)
        return SourceFetchResult(
            source = imageSource,
            mimeType = mimeType,
            dataSource = DataSource.DISK
        )
    }

    /**
     * Parse the response's `content-type` header.
     *
     * "text/plain" is often used as a default/fallback MIME type.
     * Attempt to guess a better MIME type from the file extension.
     */
    private fun getMimeType(url: String, contentType: String?): String? {
        if (contentType == null
            || contentType.trim().isEmpty()
            || contentType.startsWith(HttpUriFetcher.MIME_TYPE_TEXT_PLAIN)
        ) {
            MimeTypeMap.getMimeTypeFromUrl(url)?.let { return it }
        }
        return contentType?.substringBefore(';')
    }

    class Factory : Fetcher.Factory<Uri> {

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader
        ): CoilKotlinResourceUriFetcher? {
            return if (SCHEME.equals(data.scheme, ignoreCase = true)) {
                val resourcePath = "${data.authority.orEmpty()}${data.path.orEmpty()}"
                CoilKotlinResourceUriFetcher(
                    fileSystem = imageLoader.diskCache?.fileSystem ?: FileSystem.SYSTEM,
                    resourceName = resourcePath
                )
            } else {
                null
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Factory
        }

        override fun hashCode(): Int {
            return this@Factory::class.hashCode()
        }

        override fun toString(): String = "CoilKotlinResourceUriFetcher"
    }
}