/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.view.coil.internal

import androidx.annotation.WorkerThread
import coil.ImageLoader
import coil.fetch.SourceResult
import coil.request.CachePolicy.DISABLED
import coil.request.CachePolicy.ENABLED
import coil.request.ImageRequest
import coil.request.Options
import com.github.panpf.zoomimage.imagesource.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class CoilImageSource(
    private val imageLoader: ImageLoader,
    private val request: ImageRequest,
) : ImageSource {

    override val key: String = request.data.toString()

    @WorkerThread
    override suspend fun openInputStream(): Result<InputStream> {
        val fetcher = try {
            val options = Options(
                context = request.context,
                diskCachePolicy = ENABLED,
                networkCachePolicy = DISABLED   // Do not download image, by default go here The image have been downloaded
            )
            imageLoader.components.newFetcher(request.data, options, imageLoader)?.first
                ?: return Result.failure(IllegalStateException("Fetcher not found. data='${request.data}'"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val fetchResult = withContext(Dispatchers.IO) {
            kotlin.runCatching {
                fetcher.fetch()
            }
        }.let {
            it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!)
        }
        if (fetchResult !is SourceResult) {
            return Result.failure(IllegalStateException("FetchResult is not SourceResult. data='${request.data}'"))
        }
        return kotlin.runCatching { fetchResult.source.source().inputStream() }
    }
}