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

import coil.ImageLoader
import coil.fetch.SourceResult
import coil.request.CachePolicy.DISABLED
import coil.request.CachePolicy.ENABLED
import coil.request.ImageRequest
import coil.request.Options
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Source

class CoilImageSource(
    val data: Any,
    val imageSource: coil.decode.ImageSource
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
            // TODO support download
            val options = Options(
                context = request.context,
                diskCachePolicy = ENABLED,
                networkCachePolicy = DISABLED   // Do not download image, by default go here The image have been downloaded
            )
            val fetcher =
                imageLoader.components.newFetcher(request.data, options, imageLoader)?.first
                    ?: throw IllegalStateException("Fetcher not found. data='${request.data}'")
            val fetchResult = fetcher.fetch()
            if (fetchResult !is SourceResult) {
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