package com.github.panpf.zoomimage.util.coil

import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.pathSegments
import coil3.request.Options
import coil3.toUri
import com.github.panpf.zoomimage.subsampling.fromPhotoAsset
import okio.Buffer
import okio.FileSystem
import org.jetbrains.compose.resources.InternalResourceApi

class CoilPhotoAssetUriFetcher(
    val localIdentifier: String,
    val preferredThumbnail: Boolean,
    val allowNetworkAccess: Boolean,
) : Fetcher {

    @OptIn(InternalResourceApi::class)
    override suspend fun fetch(): FetchResult {
        val photoAssetImageSource =
            com.github.panpf.zoomimage.subsampling.ImageSource.fromPhotoAsset(
                localIdentifier = localIdentifier,
                preferredThumbnail = preferredThumbnail,
                allowNetworkAccess = allowNetworkAccess
            ).create()
        val data = photoAssetImageSource.data
        val source = ImageSource(
            source = Buffer().write(data),
            fileSystem = FileSystem.SYSTEM,
            metadata = null
        )
        val mimeType = photoAssetImageSource.mimeType
        return SourceFetchResult(
            source = source,
            mimeType = mimeType,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<Uri> {

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader
        ): CoilPhotoAssetUriFetcher? {
            val localIdentifier = parseLocalIdentifier(data.toString().toUri()) ?: return null
            return CoilPhotoAssetUriFetcher(
                localIdentifier = localIdentifier,
                preferredThumbnail = false,
                allowNetworkAccess = false
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }

        override fun toString(): String = "CoilPhotoAssetUriFetcher"
    }
}

/**
 * Check if the uri is a photos asset uri
 *
 * Support 'file:///photos_asset/DB16113B-984A-4D12-B4D0-50FC46066781/L0/001' uri
 *
 * @see com.github.panpf.sketch.core.ios.test.fetch.PhotosAssetUriFetcherTest.testIsPhotosAssetUri
 */
fun isPhotosAssetUri(uri: Uri): Boolean =
    "file".equals(uri.scheme, ignoreCase = true)
            && uri.authority?.takeIf { it.isNotEmpty() } == null
            && "photos_asset"
        .equals(uri.pathSegments.firstOrNull(), ignoreCase = true)

/**
 * Parse the local identifier from the photos asset uri
 *
 * @see com.github.panpf.sketch.core.ios.test.fetch.PhotosAssetUriFetcherTest.testParseLocalIdentifier
 */
fun parseLocalIdentifier(uri: Uri): String? =
    if (isPhotosAssetUri(uri)) {
        uri.pathSegments.drop(1).joinToString("/")
    } else {
        null
    }