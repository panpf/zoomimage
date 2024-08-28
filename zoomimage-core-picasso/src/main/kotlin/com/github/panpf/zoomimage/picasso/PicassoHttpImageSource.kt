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

package com.github.panpf.zoomimage.picasso

import android.net.Uri
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.internalDownloader
import okhttp3.Response
import okio.Source
import okio.source
import java.io.IOException

/**
 * [ImageSource] implementation that uses Picasso to load images over HTTP.
 *
 * @see com.github.panpf.zoomimage.core.picasso.test.PicassoHttpImageSourceTest
 */
class PicassoHttpImageSource(val picasso: Picasso, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override fun openSource(): Source {
        val downloaderRequest = okhttp3.Request.Builder()
            .url(uri.toString())
            .build()
        val response: Response = picasso.internalDownloader.load(downloaderRequest)
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
        return body.source().inputStream().source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PicassoHttpImageSource
        if (picasso != other.picasso) return false
        if (uri != other.uri) return false
        return true
    }

    override fun hashCode(): Int {
        var result = picasso.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

    override fun toString(): String {
        return "PicassoHttpImageSource('$uri')"
    }
}