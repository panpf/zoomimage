package com.github.panpf.zoomimage.sample.image

import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.github.panpf.sketch.fetch.parseLocalIdentifier
import com.github.panpf.sketch.util.toUri
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