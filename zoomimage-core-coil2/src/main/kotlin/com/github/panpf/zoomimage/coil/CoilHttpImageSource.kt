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

package com.github.panpf.zoomimage.coil

import android.content.Context
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.fetch.SourceResult
import coil.request.CachePolicy.ENABLED
import coil.request.Options
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Buffer
import okio.Source

/**
 * [ImageSource] implementation for Coil's HTTP requests.
 *
 * @see com.github.panpf.zoomimage.core.coil2.test.CoilHttpImageSourceTest
 */
class CoilHttpImageSource(
    private val url: String,
    private val openSourceFactory: () -> Source
) : ImageSource {

    override val key: String = url

    override fun openSource(): Source {
        return openSourceFactory.invoke()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CoilHttpImageSource
        return url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    override fun toString(): String {
        return "CoilHttpImageSource('$url')"
    }

    class Factory(
        val context: Context,
        val imageLoader: ImageLoader,
        val url: String
    ) : ImageSource.Factory {

        override val key: String = url

        @OptIn(ExperimentalCoilApi::class)
        override suspend fun create(): CoilHttpImageSource {
            val options = Options(
                context = context,
                diskCachePolicy = ENABLED,
                networkCachePolicy = ENABLED
            )
            val mappedData = imageLoader.components.map(url, options)
            val fetcher = imageLoader.components.newFetcher(mappedData, options, imageLoader)?.first
                ?: throw IllegalStateException("Fetcher not found. data='${url}'")
            val fetchResult =
                fetcher.fetch() ?: throw IllegalStateException("FetchResult is null. data='${url}'")
            if (fetchResult !is SourceResult) {
                throw IllegalStateException("FetchResult is not SourceResult. data='${url}'")
            }

            val diskCache = imageLoader.diskCache
            val openSourceFactory1 = diskCache?.openSnapshot(url)?.use {
                val path = it.data
                CoilHttpImageSource(url) {
                    diskCache.fileSystem.source(path)
                }
            }
            if (openSourceFactory1 != null) {
                @Suppress("BlockingMethodInNonBlockingContext")
                fetchResult.source.close()
                return openSourceFactory1
            }

            val byteArray = fetchResult.source.use {
                it.source().readByteArray()
            }
            return CoilHttpImageSource(url) {
                Buffer().write(byteArray)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Factory
            if (imageLoader != other.imageLoader) return false
            if (url != other.url) return false
            return true
        }

        override fun hashCode(): Int {
            var result = imageLoader.hashCode()
            result = 31 * result + url.hashCode()
            return result
        }

        override fun toString(): String {
            return "CoilHttpImageSource.Factory('$url')"
        }
    }
}