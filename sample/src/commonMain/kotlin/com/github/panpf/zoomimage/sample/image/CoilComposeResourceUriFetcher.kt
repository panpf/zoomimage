package com.github.panpf.zoomimage.sample.image

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
import com.github.panpf.zoomimage.sample.util.ThrowingFileSystem
import okio.Buffer
import okio.FileSystem
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

class CoilComposeResourceUriFetcher(
    val fileSystem: FileSystem,
    val resourcePath: String,
) : Fetcher {

    companion object {
        const val SCHEME = "compose.resource"
    }

    @OptIn(InternalResourceApi::class)
    override suspend fun fetch(): FetchResult {
        val bytes = readResourceBytes(resourcePath)
        val source = ImageSource(
            source = Buffer().write(bytes),
            fileSystem = fileSystem,
            metadata = null
        )
        val mimeType = getMimeType(resourcePath, null)
        return SourceFetchResult(
            source = source,
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
        ): CoilComposeResourceUriFetcher? {
            return if (SCHEME.equals(data.scheme, ignoreCase = true)) {
                val resourcePath = "${data.authority.orEmpty()}${data.path.orEmpty()}"
                CoilComposeResourceUriFetcher(
                    fileSystem = imageLoader.diskCache?.fileSystem ?: ThrowingFileSystem,
                    resourcePath = resourcePath
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

        override fun toString(): String = "CoilComposeResourceUriFetcher"
    }
}