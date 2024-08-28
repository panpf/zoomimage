/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.glide

import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.Encoder
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.data.HttpUrlFetcher
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.internalDiskCache
import com.bumptech.glide.load.model.ByteBufferEncoder
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.Source
import okio.buffer
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * [ImageSource] implementation for Glide's HTTP requests.
 *
 * @see com.github.panpf.zoomimage.core.glide.test.GlideHttpImageSourceTest
 */
class GlideHttpImageSource(
    val glideUrl: GlideUrl,
    val openSource: () -> Source
) : ImageSource {

    override val key: String = glideUrl.toString()

    override fun openSource(): Source {
        return openSource.invoke()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GlideHttpImageSource
        return glideUrl == other.glideUrl
    }

    override fun hashCode(): Int {
        return glideUrl.hashCode()
    }

    override fun toString(): String {
        return "GlideHttpImageSource('$glideUrl')"
    }

    class Factory(
        val glide: Glide,
        val glideUrl: GlideUrl
    ) : ImageSource.Factory {

        override val key: String = glideUrl.toString()

        constructor(glide: Glide, imageUri: String) : this(glide, GlideUrl(imageUri))

        private val diskCache by lazy { glide.internalDiskCache }

        override suspend fun create(): GlideHttpImageSource {
            val diskCache = diskCache
            val file = diskCache?.get(glideUrl)
            if (file != null) {
                return GlideHttpImageSource(glideUrl) {
                    FileInputStream(file).source()
                }
            }

            val data: InputStream = suspendCoroutine { continuation ->
                val fetcher = HttpUrlFetcher(glideUrl, HttpGlideUrlLoader.TIMEOUT.defaultValue!!)
                fetcher.loadData(
                    /* priority = */ Priority.IMMEDIATE,
                    /* callback = */ object : DataFetcher.DataCallback<InputStream> {
                        override fun onDataReady(data: InputStream?) {
                            if (data != null) {
                                continuation.resume(Result.success(data))
                            } else {
                                continuation.resumeWithException(FileNotFoundException("Data is null"))
                            }
                        }

                        override fun onLoadFailed(e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                )
            }.getOrThrow()

            val bytes = withContext(ioCoroutineDispatcher()) {
                // It is impossible to accurately determine whether the current disk cache is available,
                // so we can only read it into the memory first and then try to write it to the disk.
                data.source().buffer().use { it.readByteArray() }.apply {
                    diskCache?.put(
                        /* key = */ glideUrl,
                        /* writer = */ DataCacheWriter(
                            encoder = ByteBufferEncoder(),
                            data = ByteBuffer.wrap(this@apply),
                            options = Options()
                        )
                    )
                }
            }
            return GlideHttpImageSource(glideUrl) { bytes.inputStream().source() }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Factory
            if (glide != other.glide) return false
            if (glideUrl != other.glideUrl) return false
            return true
        }

        override fun hashCode(): Int {
            var result = glide.hashCode()
            result = 31 * result + glideUrl.hashCode()
            return result
        }

        override fun toString(): String {
            return "GlideHttpImageSource.Factory('$glideUrl')"
        }

        private class DataCacheWriter<DataType>(
            private val encoder: Encoder<DataType>,
            private val data: DataType,
            private val options: Options
        ) : DiskCache.Writer {

            override fun write(file: File): Boolean {
                @Suppress("UNCHECKED_CAST")
                return encoder.encode(data as (DataType & Any), file, options)
            }
        }
    }
}