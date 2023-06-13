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
package com.github.panpf.zoomimage.internal

import android.net.Uri
import com.github.panpf.zoomimage.core.ImageSource
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

class PicassoHttpImageSource(val picasso: Picasso, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override suspend fun openInputStream(): Result<InputStream> {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val downloaderRequest = okhttp3.Request.Builder()
                    .url(uri.toString())
                    .cacheControl(CacheControl.FORCE_CACHE) // Do not download image, by default go here The image have been downloaded
                    .build()
                val response: Response = picasso.downloader.load(downloaderRequest)
                val body =
                    response.body() ?: throw IOException("HTTP response body is null. uri='$uri'")

                if (!response.isSuccessful) {
                    body.close()
                    throw IOException("HTTP ${response.code()} ${response.message()}. uri='$uri'")
                }

                // Cache response is only null when the response comes fully from the network. Both completely
                // cached and conditionally cached responses will have a non-null cache response.

                // Cache response is only null when the response comes fully from the network. Both completely
                // cached and conditionally cached responses will have a non-null cache response.
                val loadedFrom =
                    if (response.cacheResponse() == null) LoadedFrom.NETWORK else LoadedFrom.DISK

                // Sometimes response content length is zero when requests are being replayed. Haven't found
                // root cause to this but retrying the request seems safe to do so.

                // Sometimes response content length is zero when requests are being replayed. Haven't found
                // root cause to this but retrying the request seems safe to do so.
                if (loadedFrom == LoadedFrom.DISK && body.contentLength() == 0L) {
                    body.close()
                    throw IOException("Received response with 0 content-length header. uri='$uri'")
                }
                body.source().inputStream()
            }
        }
    }
}