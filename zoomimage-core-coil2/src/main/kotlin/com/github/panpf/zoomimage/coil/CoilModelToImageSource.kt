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

import com.github.panpf.zoomimage.subsampling.ImageSource as ZoomImageImageSource
import android.content.Context
import coil.ImageLoader
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import okhttp3.HttpUrl
import okio.Buffer
import okio.Path.Companion.toPath
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.File
import java.nio.ByteBuffer

/**
 * Convert coil model to [ZoomImageImageSource.Factory]
 */
interface CoilModelToImageSource {

    fun dataToImageSource(
        context: Context,
        imageLoader: ImageLoader,
        model: Any
    ): ZoomImageImageSource.Factory?
}

/**
 * Default implementation of [CoilModelToImageSource]
 *
 * @see com.github.panpf.zoomimage.core.coil2.test.CoilModelToImageSourceImplTest
 */
class CoilModelToImageSourceImpl : CoilModelToImageSource {

    override fun dataToImageSource(
        context: Context,
        imageLoader: ImageLoader,
        model: Any
    ): ImageSource.Factory? {
        val uri = when (model) {
            is String -> android.net.Uri.parse(model)
            is android.net.Uri -> model
            else -> null
        }
        return when {
            model is HttpUrl && (model.scheme == "http" || model.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            uri != null && (uri.scheme == "http" || uri.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            uri != null && uri.scheme == "content" -> {
                val androidUri = android.net.Uri.parse(model.toString())
                ImageSource.fromContent(context, androidUri).toFactory()
            }

            // file:///android_asset/image.jpg
            uri != null && uri.scheme == "file" && uri.pathSegments.firstOrNull() == "android_asset" -> {
                val assetFileName = uri.pathSegments.drop(1).joinToString("/")
                ImageSource.fromAsset(context, assetFileName).toFactory()
            }

            // /sdcard/xxx.jpg
            uri != null && uri.scheme?.takeIf { it.isNotEmpty() } == null
                    && uri.authority?.takeIf { it.isNotEmpty() } == null
                    && uri.path?.startsWith("/") == true -> {
                ImageSource.fromFile(uri.path!!.toPath()).toFactory()
            }

            // file:///sdcard/xxx.jpg
            uri != null && uri.scheme == "file"
                    && uri.authority?.takeIf { it.isNotEmpty() } == null
                    && uri.path?.startsWith("/") == true -> {
                ImageSource.fromFile(uri.path!!.toPath()).toFactory()
            }

            model is File -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is Int -> {
                ImageSource.fromResource(context, model).toFactory()
            }

            // android.resource://example.package.name/drawable/image
            uri != null && uri.scheme == "android.resource" && uri.pathSegments.size == 2 -> {
                val packageName = uri.authority?.takeIf { it.isNotEmpty() } ?: context.packageName
                val resources = context.packageManager.getResourcesForApplication(packageName)
                val (type, name) = uri.pathSegments
                //noinspection DiscouragedApi: Necessary to support resource URIs.
                val id = resources.getIdentifier(name, type, packageName)
                ImageSource.fromResource(resources, id).toFactory()
            }

            // android.resource://example.package.name/4125123
            uri != null && uri.scheme == "android.resource" && uri.pathSegments.size == 1 -> {
                val packageName = uri.authority?.takeIf { it.isNotEmpty() } ?: context.packageName
                val resources = context.packageManager.getResourcesForApplication(packageName)
                val id = uri.pathSegments.first().toInt()
                ImageSource.fromResource(resources, id).toFactory()
            }

            model is ByteArray -> {
                ImageSource.fromByteArray(model).toFactory()
            }

            model is ByteBuffer -> {
                val byteArray: ByteArray = model.asSource().buffer().use { it.readByteArray() }
                ImageSource.fromByteArray(byteArray).toFactory()
            }

            else -> {
                null
            }
        }
    }

    internal fun ByteBuffer.asSource() = object : Source {
        private val buffer = this@asSource.slice()
        private val len = buffer.capacity()

        override fun close() = Unit

        override fun read(sink: Buffer, byteCount: Long): Long {
            if (buffer.position() == len) return -1
            val pos = buffer.position()
            val newLimit = (pos + byteCount).toInt().coerceAtMost(len)
            buffer.limit(newLimit)
            return sink.write(buffer).toLong()
        }

        override fun timeout() = Timeout.NONE
    }
}