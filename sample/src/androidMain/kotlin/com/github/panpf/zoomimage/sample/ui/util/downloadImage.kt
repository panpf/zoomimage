package com.github.panpf.zoomimage.sample.ui.util

import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.internal.EmptyDiskCache
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.util.buildDownloadRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import okio.Buffer
import okio.FileSystem
import okio.Path

suspend fun Sketch.download(uri: String): DownloadResult = coroutineScope {
    val dataResult = kotlin.runCatching {
        if (downloadCache !is EmptyDiskCache) {
            downloadCache.withLock(uri) {
                val request = buildDownloadRequest(context, uri)
                @Suppress("MoveVariableDeclarationIntoWhen") val result = execute(request)
                when (result) {
                    is ImageResult.Success -> {
                        val cachedPath = downloadCache.openSnapshot(uri)!!.use { it.data }
                        DownloadData.Cache(fileSystem, cachedPath)
                    }

                    is ImageResult.Error -> {
                        throw result.throwable
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown result")
                    }
                }
            }
        } else {
            val response = httpStack.getResponse(url = uri, httpHeaders = null, extras = null)
            response.content().use { content ->
                val sink = Buffer()
                var bytesCopied = 0L
                val buffer = ByteArray(1024 * 8)
                var bytes = content.read(buffer)
                while (bytes >= 0 && isActive) {
                    sink.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    bytes = content.read(buffer)
                }
                if (!isActive) {
                    throw CancellationException("Canceled")
                }
                DownloadData.Bytes(sink.readByteArray())
            }
        }
    }
    val data = dataResult.getOrNull()
    if (data != null) {
        DownloadResult.Success(uri, data)
    } else {
        DownloadResult.Error(uri, dataResult.exceptionOrNull()!!)
    }
}

interface DownloadResult {
    val uri: String

    data class Success constructor(
        override val uri: String,
        val data: DownloadData,
    ) : DownloadResult

    data class Error constructor(
        override val uri: String,
        val throwable: Throwable,
    ) : DownloadResult
}

interface DownloadData {
    class Bytes(val bytes: ByteArray) : DownloadData
    class Cache(val fileSystem: FileSystem, val path: Path) : DownloadData
}