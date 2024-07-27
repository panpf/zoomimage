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
import coil3.PlatformContext
import coil3.pathSegments
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import okio.Path
import java.io.File
import java.net.URL

/**
 * Convert coil model to [ImageSource.Factory] for Android platform
 *
 * @see com.github.panpf.zoomimage.core.coil.android.test.CoilModelToImageSourceImplTest
 */
actual class CoilModelToImageSourceImpl actual constructor(
    private val context: PlatformContext,
    private val imageLoader: ImageLoader
) : CoilModelToImageSource {

    actual override fun dataToImageSource(model: Any): ImageSource.Factory? {
        return when {
            model is URL -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is android.net.Uri && (model.scheme == "http" || model.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is coil3.Uri && (model.scheme == "http" || model.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is String && model.startsWith("content://") -> {
                ImageSource.fromContent(context, android.net.Uri.parse(model)).toFactory()
            }

            model is android.net.Uri && model.scheme == "content" -> {
                ImageSource.fromContent(context, model).toFactory()
            }

            model is coil3.Uri && model.scheme == "content" -> {
                ImageSource.fromContent(context, android.net.Uri.parse(model.toString()))
                    .toFactory()
            }

            model is String && model.startsWith("file:///android_asset/") -> {
                val assetFileName = android.net.Uri.parse(model).pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is android.net.Uri && model.scheme == "file" && model.pathSegments.firstOrNull() == "android_asset" -> {
                val assetFileName = model.pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is coil3.Uri && model.scheme == "file" && model.pathSegments.firstOrNull() == "android_asset" -> {
                val assetFileName = model.pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is Path -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is String && model.startsWith("/") -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is String && model.startsWith("file://") -> {
                val filePath = android.net.Uri.parse(model).path
                filePath?.let { ImageSource.fromFile(File(filePath)).toFactory() }
            }

            model is android.net.Uri && model.scheme == "file" -> {
                val filePath = model.path
                filePath?.let { ImageSource.fromFile(File(filePath)).toFactory() }
            }

            model is coil3.Uri && model.scheme == "file" -> {
                val filePath = model.path
                filePath?.let { ImageSource.fromFile(File(filePath)).toFactory() }
            }

            model is File -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is Int -> {
                ImageSource.fromResource(context, model).toFactory()
            }

            model is ByteArray -> {
                ImageSource.fromByteArray(model).toFactory()
            }

            else -> {
                null
            }
        }
    }
}