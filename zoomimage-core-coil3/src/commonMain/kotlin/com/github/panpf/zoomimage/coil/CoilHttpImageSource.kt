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

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.fetch.SourceFetchResult
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.Options
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Buffer
import okio.Source

/**
 * [ImageSource] implementation for Coil's HTTP requests.
 *
 * @see com.github.panpf.zoomimage.core.coil3.desktop.test.CoilHttpImageSourceTest
 */
@Suppress("RedundantConstructorKeyword")
class CoilHttpImageSource constructor(
    val url: String,
    val openSourceFactory: () -> Source
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

    /**
     * @see com.github.panpf.zoomimage.core.coil3.desktop.test.CoilHttpImageSourceFactoryTest
     */
    class Factory constructor(
        val context: PlatformContext,
        val imageLoader: ImageLoader,
        val request: ImageRequest
    ) : ImageSource.Factory {

        @Deprecated("Please use constructor(context, imageLoader, request) instead")
        constructor(
            context: PlatformContext,
            imageLoader: ImageLoader,
            url: String
        ) : this(
            context = context,
            imageLoader = imageLoader,
            request = ImageRequest.Builder(context)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build()
        )

        val url = request.data.toString()

        override val key: String = request.data.toString()

        override suspend fun create(): CoilHttpImageSource {
            val options = Options(
                context = context,
                diskCachePolicy = request.diskCachePolicy,
                networkCachePolicy = request.networkCachePolicy,
                diskCacheKey = request.diskCacheKey,
                fileSystem = request.fileSystem,
                extras = request.extras
            )
            val mappedData = imageLoader.components.map(request.data, options)

            val fetcher = imageLoader.components.newFetcher(mappedData, options, imageLoader)?.first
                ?: throw IllegalStateException("Fetcher not found. data='${url}'")
            val fetchResult =
                fetcher.fetch() ?: throw IllegalStateException("FetchResult is null. data='${url}'")
            if (fetchResult !is SourceFetchResult) {
                throw IllegalStateException("FetchResult is not SourceFetchResult. data='${url}'")
            }

            val diskCache = imageLoader.diskCache
            val coilHttpImageSource = diskCache?.openSnapshot(url)?.use {
                val path = it.data
                CoilHttpImageSource(url) {
                    diskCache.fileSystem.source(path)
                }
            }
            if (coilHttpImageSource != null) {
                try {
                    fetchResult.source.close()
                } catch (e: RuntimeException) {
                    throw e
                } catch (_: Exception) {
                }
                return coilHttpImageSource
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
            if (request != other.request) return false
            return true
        }

        override fun hashCode(): Int {
            var result = imageLoader.hashCode()
            result = 31 * result + request.hashCode()
            return result
        }

        override fun toString(): String {
            return "CoilHttpImageSource.Factory(${request.data})"
        }
    }
}