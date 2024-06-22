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

package com.github.panpf.zoomimage.picasso

import android.content.Context
import android.net.Uri
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromResource
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.downloader
import okhttp3.CacheControl
import okhttp3.Response
import okio.Path.Companion.toOkioPath
import okio.Source
import okio.source
import java.io.File
import java.io.IOException

fun newPicassoImageSource(context: Context, uri: Uri?): ImageSource? {
    uri ?: return null
    return when {
        uri.scheme == "http" || uri.scheme == "https" -> {
            PicassoHttpImageSource(Picasso.get(), uri)
        }

        uri.scheme == "content" -> {
            ImageSource.fromContent(context, uri)
        }

        uri.scheme == "file" && uri.pathSegments.firstOrNull() == "android_asset" -> {
            val assetFileName = uri.pathSegments
                .takeIf { it.size > 1 }
                ?.let { it.subList(1, it.size) }
                ?.joinToString(separator = "/")
            assetFileName?.let { ImageSource.fromAsset(context, it) }
        }

        uri.scheme == "file" -> {
            val filePath = uri.path
            filePath?.let { ImageSource.fromFile(File(filePath).toOkioPath()) }
        }

        uri.scheme == "android.resource" -> {
            val resId = uri.authority?.toIntOrNull()
            resId?.let { ImageSource.fromResource(context, it) }
        }

        else -> {
            null
        }
    }
}


class PicassoHttpImageSource(val picasso: Picasso, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override fun openSource(): Result<Source> = kotlin.runCatching {
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
        body.source().inputStream().source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PicassoHttpImageSource) return false
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