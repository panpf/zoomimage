package com.github.panpf.zoomimage.sample.ui.test

import com.githb.panpf.zoomimage.images.DesktopLocalImages
import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.fetch.ComposeResourceUriFetcher
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.fetch.FileUriFetcher
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.fetch.KotlinResourceUriFetcher
import com.github.panpf.sketch.source.ByteArrayDataSource
import com.github.panpf.sketch.source.FileDataSource
import com.github.panpf.sketch.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.sample.data.ComposeResourceImages
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.withContext
import okio.buffer

actual suspend fun getImageSourceTestItems(context: PlatformContext): List<Pair<String, String>> {
    return listOf(
        "FILE" to DesktopLocalImages.with().cat.uri,
        "BYTES" to HttpImages.hugeLongComic.uri,
        "RES_KOTLIN" to ResourceImages.dog.uri,
        "RES_COMPOSE" to ComposeResourceImages.hugeChina.uri,
    )
}

actual suspend fun sketchFetcherToZoomImageImageSource(
    context: PlatformContext,
    fetcher: Fetcher,
    http2ByteArray: Boolean
): ImageSource.Factory? =
    when (fetcher) {
        is FileUriFetcher -> {
            ImageSource.fromFile(fetcher.path).toFactory()
        }

        is HttpUriFetcher -> {
            val fetchResult = withContext(ioCoroutineDispatcher()) {
                fetcher.fetch()
            }.getOrThrow()
            val dataSource = fetchResult.dataSource
            if (dataSource is FileDataSource) {
                if (http2ByteArray) {
                    withContext(ioCoroutineDispatcher()) {
                        val bytes = dataSource.fileSystem.source(dataSource.path).buffer()
                            .use { it.readByteArray() }
                        ImageSource.fromByteArray(bytes)
                    }
                } else {
                    ImageSource.fromFile(dataSource.path)
                }
            } else {
                ImageSource.fromByteArray((dataSource as ByteArrayDataSource).data)
            }.toFactory()
        }

        is KotlinResourceUriFetcher -> {
            ImageSource.fromKotlinResource(fetcher.resourcePath).toFactory()
        }

        is ComposeResourceUriFetcher -> {
            ComposeResourceImageSource.Factory(fetcher.resourcePath)
        }

        else -> null
    }