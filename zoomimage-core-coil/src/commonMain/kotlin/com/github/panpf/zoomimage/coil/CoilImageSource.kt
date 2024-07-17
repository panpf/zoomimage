/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import coil3.fetch.SourceFetchResult
import coil3.request.CachePolicy.ENABLED
import coil3.request.ImageRequest
import coil3.request.Options
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Source

class CoilImageSource(
    val data: Any,
    val imageSource: coil3.decode.ImageSource
) : ImageSource {

    override val key: String = data.toString()

    override fun openSource(): Source {
        return imageSource.source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoilImageSource) return false
        if (data != other.data) return false
        if (imageSource != other.imageSource) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + imageSource.hashCode()
        return result
    }

    override fun toString(): String {
        return "CoilImageSource('${data}')"
    }

    class Factory(
        val imageLoader: ImageLoader,
        val request: ImageRequest,
    ) : ImageSource.Factory {

        override val key: String = request.data.toString()

        override suspend fun create(): CoilImageSource {
            val options = Options(
                context = request.context,
                diskCachePolicy = ENABLED,
                networkCachePolicy = ENABLED
            )
            val mappedData = imageLoader.components.map(request.data, options)
            val fetcher =
                imageLoader.components.newFetcher(mappedData, options, imageLoader)?.first
                    ?: throw IllegalStateException("Fetcher not found. data='${request.data}'")
            val fetchResult = fetcher.fetch()
            if (fetchResult !is SourceFetchResult) {
                throw IllegalStateException("FetchResult is not SourceResult. data='${request.data}'")
            }
            val imageSource = fetchResult.source
            return CoilImageSource(request.data, imageSource)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Factory) return false
            if (imageLoader != other.imageLoader) return false
            if (request.data != other.request.data) return false
            return true
        }

        override fun hashCode(): Int {
            var result = imageLoader.hashCode()
            result = 31 * result + request.data.hashCode()
            return result
        }

        override fun toString(): String {
            return "CoilImageSource.Factory('${request.data}')"
        }
    }
}