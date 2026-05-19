package com.github.panpf.zoomimage.sample.ui.components

import com.github.panpf.sketch.annotation.WorkerThread
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.cache.DiskCache
import com.github.panpf.sketch.cache.isReadAndWrite
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.fetch.PhotosAssetFetcher
import com.github.panpf.sketch.fetch.parseLocalIdentifier
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.request.allowNetworkAccessPhotosAsset
import com.github.panpf.sketch.request.preferFileCacheForImagePhotosAsset
import com.github.panpf.sketch.request.preferThumbnailForPhotosAsset
import com.github.panpf.sketch.request.useSkiaForImagePhotosAsset
import com.github.panpf.sketch.source.ByteArrayDataSource
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.sketch.source.DataSource
import com.github.panpf.sketch.source.FileDataSource
import com.github.panpf.sketch.source.PhotosAssetDataSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.Buffer
import okio.IOException
import okio.Path
import okio.buffer
import okio.use
import platform.Photos.PHAssetResourceManager
import platform.Photos.PHAssetResourceRequestOptions
import platform.darwin.ByteVar
import platform.posix.memcpy
import kotlin.coroutines.resumeWithException

/**
 * PhotosAssetFetcher is a Fetcher that fetches photo assets from the iOS Photos library using their local identifiers. It supports fetching both the original and thumbnail versions of the assets, and can optionally use Skia for image processing.
 *
 * @see com.github.panpf.sketch.core.ios.test.fetch.PhotosAssetUriFetcherTest
 */
class MyPhotosAssetFetcher(
    val downloadCache: DiskCache,
    val localIdentifier: String,
    val preferredThumbnail: Boolean,
    val allowNetworkAccess: Boolean,
    val useSkiaForImagePhotosAsset: Boolean,
    val preferFileCacheForImagePhotosAsset: Boolean,
    val downloadCachePolicy: CachePolicy,
) : Fetcher {

    companion object {
        const val SCHEME = "file"
        const val PATH_ROOT = "photos_asset"
        const val SORT_WEIGHT = PhotosAssetFetcher.SORT_WEIGHT - 1
    }

    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = runCatching {
        val asset = fetchPhotosAsset(localIdentifier)
            ?: throw IOException("Not found PHAsset: '$localIdentifier'")
        val resource = selectPrimaryResource(asset, preferredThumbnail)
            ?: throw IOException("Not found PHAssetResource: '$localIdentifier'")
        val mimeType = resolveMimeType(resource) ?: resolveMimeType(asset)
        val dataSource = PhotosAssetDataSource(
            localIdentifier = localIdentifier,
            preferredThumbnail = preferredThumbnail,
            allowNetworkAccess = allowNetworkAccess,
            asset = asset,
            resource = resource,
        ).let {
            tryConvertDataSource(it, mimeType)
        }
        FetchResult(dataSource, mimeType)
    }

    private suspend fun tryConvertDataSource(
        dataSource: PhotosAssetDataSource,
        mimeType: String?
    ): DataSource {
        val useSkiaForImagePhotosAsset = useSkiaForImagePhotosAsset ?: false
        if (!shouldUseSkia(mimeType, useSkiaForImagePhotosAsset)) {
            return dataSource
        }

        val result = runCatching {
            // Currently, both SkiaDecoder and SkiaAnimatedDecoder need to load all the image data into memory before decoding.
            // So it makes no sense to cache the original image data locally and then read it again.
            // If SkiaDecoder and SkiaAnimatedDecoder support streaming decoding later,
            // By default, locally cached files can be used first for decoding to avoid taking up too much memory.
            val preferFileCacheForImagePhotosAsset =
                preferFileCacheForImagePhotosAsset ?: false
            if (preferFileCacheForImagePhotosAsset) {
                val cachePolicy = downloadCachePolicy
                val (cachePath, dataFrom) =
                    getCacheFile(dataSource, cachePolicy)
                FileDataSource(cachePath, downloadCache.fileSystem, dataFrom)
            } else {
                val bytes = getBytes(dataSource)
                ByteArrayDataSource(data = bytes, dataFrom = DataFrom.LOCAL)
            }
        }
        return result.getOrThrow()
    }

    private fun shouldUseSkia(mimeType: String?, useSkiaForImagePhotosAsset: Boolean): Boolean {
        if (mimeType == null) return false
        // PhotosAssetDecoder does not support gif and animated webp, just use skia to decode it.
        if (mimeType == "image/gif" || mimeType == "image/webp") return true
        return useSkiaForImagePhotosAsset && mimeType.startsWith("image/")
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun getBytes(dataSource: PhotosAssetDataSource): ByteArray {
        return suspendCancellableCoroutine { continuation ->
            val buffer = Buffer()
            val options = PHAssetResourceRequestOptions().apply {
                this.networkAccessAllowed = dataSource.allowNetworkAccess
            }
            PHAssetResourceManager.defaultManager().requestDataForAssetResource(
                resource = dataSource.resource,
                options = options,
                dataReceivedHandler = { chunk ->
                    val byteVars = chunk?.bytes?.reinterpret<ByteVar>()
                    if (byteVars != null) {
                        val byteArray = ByteArray(chunk.length.toInt())
                        byteArray.usePinned { pinned ->
                            memcpy(pinned.addressOf(0), byteVars, chunk.length)
                        }
                        buffer.write(byteArray)
                    }
                },
                completionHandler = { error ->
                    val byteArray = buffer.readByteArray()
                    if (byteArray.isNotEmpty()) {
                        continuation.resumeWith(Result.success(byteArray))
                    } else {
                        val message =
                            "Failed get bytes for PHAssetResource '${dataSource.resource.originalFilename}': ${error?.localizedDescription}"
                        continuation.resumeWithException(IOException(message))
                    }
                },
            )
        }
    }

    private suspend fun getCacheFile(
        dataSource: PhotosAssetDataSource,
        cachePolicy: CachePolicy
    ): Pair<Path, DataFrom> {
        if (!cachePolicy.isReadAndWrite) {
            throw Exception("Cache policy disabled read and write")
        }

        val cacheKey = dataSource.key
        return downloadCache.withLock(cacheKey) {
            readCache(downloadCache, cacheKey)?.let { it to DataFrom.DOWNLOAD_CACHE }
                ?: (writeCache(downloadCache, cacheKey, dataSource) to DataFrom.LOCAL)
        }
    }

    @WorkerThread
    private fun readCache(downloadCache: DiskCache, cacheKey: String): Path? {
        return downloadCache.openSnapshot(cacheKey)?.use { it.data }
    }

    @OptIn(ExperimentalForeignApi::class)
    @WorkerThread
    private suspend fun writeCache(
        downloadCache: DiskCache,
        cacheKey: String,
        dataSource: PhotosAssetDataSource
    ): Path {
        val editor = downloadCache.openEditor(cacheKey)
            ?: throw IOException("Disk cache cannot be used")
        val sink = downloadCache.fileSystem.sink(editor.data).buffer()
        try {
            suspendCancellableCoroutine { continuation ->
                val options = PHAssetResourceRequestOptions().apply {
                    this.networkAccessAllowed = dataSource.allowNetworkAccess
                }
                PHAssetResourceManager.defaultManager().requestDataForAssetResource(
                    resource = dataSource.resource,
                    options = options,
                    dataReceivedHandler = { chunk ->
                        val byteVars = chunk?.bytes?.reinterpret<ByteVar>()
                        if (byteVars != null) {
                            val byteArray = ByteArray(chunk.length.toInt())
                            byteArray.usePinned { pinned ->
                                memcpy(pinned.addressOf(0), byteVars, chunk.length)
                            }
                            sink.write(byteArray)
                        }
                    },
                    completionHandler = { error ->
                        if (error == null) {
                            continuation.resumeWith(Result.success(true))
                        } else {
                            val message =
                                "Failed to write PHAssetResource '${dataSource.resource.originalFilename}' to cache: ${error.localizedDescription}"
                            continuation.resumeWithException(IOException(message))
                        }
                    },
                )
            }
            editor.commit()
            return editor.data
        } catch (t: Throwable) {
            editor.abort()
            throw t
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PhotosAssetFetcher
        if (localIdentifier != other.localIdentifier) return false
        if (preferredThumbnail != other.preferredThumbnail) return false
        if (allowNetworkAccess != other.allowNetworkAccess) return false
        return true
    }

    override fun hashCode(): Int {
        var result = localIdentifier.hashCode()
        result = 31 * result + preferredThumbnail.hashCode()
        result = 31 * result + allowNetworkAccess.hashCode()
        return result
    }

    override fun toString(): String {
        return "MyPhotosAssetFetcher(localIdentifier='$localIdentifier', preferredThumbnail=$preferredThumbnail, allowNetworkAccess=$allowNetworkAccess)"
    }

    class Factory : Fetcher.Factory {

        override val sortWeight: Int = SORT_WEIGHT

        override fun create(requestContext: RequestContext): Fetcher? {
            val request = requestContext.request
            val uri = request.uri
            val localIdentifier = parseLocalIdentifier(uri) ?: return null
            val preferredThumbnail = request.preferThumbnailForPhotosAsset ?: false
            val allowNetworkAccess = request.allowNetworkAccessPhotosAsset ?: false
            val useSkiaForImagePhotosAsset = request.useSkiaForImagePhotosAsset ?: false
            val preferFileCacheForImagePhotosAsset =
                request.preferFileCacheForImagePhotosAsset ?: false
            val downloadCachePolicy = request.downloadCachePolicy
            return MyPhotosAssetFetcher(
                downloadCache = requestContext.sketch.downloadCache,
                localIdentifier = localIdentifier,
                preferredThumbnail = preferredThumbnail,
                allowNetworkAccess = allowNetworkAccess,
                useSkiaForImagePhotosAsset = useSkiaForImagePhotosAsset,
                preferFileCacheForImagePhotosAsset = preferFileCacheForImagePhotosAsset,
                downloadCachePolicy = downloadCachePolicy,
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other != null && this::class == other::class
        }

        override fun hashCode(): Int = this::class.hashCode()

        override fun toString(): String = "MyPhotosAssetFetcher"
    }
}