package com.github.panpf.zoomimage.images.coil

import coil3.ImageLoader
import coil3.Uri
import coil3.annotation.InternalCoilApi
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.pathSegments
import coil3.request.Options
import coil3.toUri
import coil3.util.MimeTypeMap
import com.github.panpf.zoomimage.subsampling.KotlinResourceImageSource
import okio.FileSystem
import okio.buffer

class CoilKotlinResourceUriFetcher(
    val fileSystem: FileSystem,
    val resourceName: String,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val imageSource = ImageSource(
            source = KotlinResourceImageSource(resourceName).openSource().buffer(),
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
    @OptIn(InternalCoilApi::class)
    private fun getMimeType(url: String, contentType: String?): String? {
        if (contentType == null
            || contentType.trim().isEmpty()
            || contentType.startsWith("text/plain")
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
            if (!isKotlinResourceUri(data.toString().toUri())) return null
            val resourcePath = data.pathSegments.drop(1).joinToString("/")
            return CoilKotlinResourceUriFetcher(
                fileSystem = imageLoader.diskCache?.fileSystem ?: FileSystem.SYSTEM,
                resourceName = resourcePath
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String = "CoilKotlinResourceUriFetcher"
    }
}