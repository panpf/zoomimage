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
import coil3.request.CachePolicy.DISABLED
import coil3.request.CachePolicy.ENABLED
import coil3.request.ImageRequest
import coil3.request.Options
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.Source

class CoilImageSource(
    private val imageLoader: ImageLoader,
    private val request: ImageRequest,
) : ImageSource {

    override val key: String = request.data.toString()

    override suspend fun openSource(): Result<Source> = withContext(ioCoroutineDispatcher()) {
        kotlin.runCatching {
            val options = Options(
                context = request.context,
                diskCachePolicy = ENABLED,
                networkCachePolicy = DISABLED   // Do not download image, by default go here The image have been downloaded
            )
            val mappedData = imageLoader.components.map(request.data, options)
            val fetcher =
                imageLoader.components.newFetcher(mappedData, options, imageLoader)?.first
                    ?: throw IllegalStateException("Fetcher not found. data='${request.data}'")
            val fetchResult = fetcher.fetch()
            if (fetchResult !is SourceFetchResult) {
                throw IllegalStateException("FetchResult is not SourceResult. data='${request.data}'")
            }
            fetchResult.source.source()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoilImageSource) return false
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
        return "CoilImageSource('${request.data}')"
    }
}