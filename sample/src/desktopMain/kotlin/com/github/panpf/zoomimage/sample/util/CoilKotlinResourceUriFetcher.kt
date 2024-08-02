package com.github.panpf.zoomimage.sample.util

import androidx.annotation.WorkerThread
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ResourceLoader
import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.pathSegments
import coil3.request.Options
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.fetch.isKotlinResourceUri
import com.github.panpf.sketch.util.MimeTypeMap
import com.github.panpf.sketch.util.toUri
import okio.FileSystem
import okio.buffer
import okio.source

class CoilKotlinResourceUriFetcher(
    val fileSystem: FileSystem,
    val resourceName: String,
) : Fetcher {

    @OptIn(ExperimentalComposeUiApi::class)
    @WorkerThread
    override suspend fun fetch(): FetchResult {
        val source = ImageSource(
            source = ResourceLoader.Default.load(resourceName).source().buffer(),
            fileSystem = fileSystem,
            metadata = null
        )
        val mimeType = getMimeType(resourceName, null)
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
            return other is Factory
        }

        override fun hashCode(): Int {
            return this@Factory::class.hashCode()
        }

        override fun toString(): String = "CoilKotlinResourceUriFetcher"
    }
}