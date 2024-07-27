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

package com.github.panpf.zoomimage.glide

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import java.io.File
import java.net.URL

/**
 * Convert the glide model to [ImageSource.Factory]
 */
interface GlideModelToImageSource {
    fun dataToImageSource(model: Any): ImageSource.Factory?
}

/**
 * Default implementation of [GlideModelToImageSource]
 *
 * @see com.github.panpf.zoomimage.core.glide.test.GlideModelToImageSourceImplTest
 */
class GlideModelToImageSourceImpl(private val context: Context) : GlideModelToImageSource {

    override fun dataToImageSource(model: Any): ImageSource.Factory? {
        return when {
            model is GlideUrl -> {
                GlideHttpImageSource.Factory(Glide.get(context), model)
            }

            model is URL -> {
                GlideHttpImageSource.Factory(Glide.get(context), GlideUrl(model))
            }

            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                GlideHttpImageSource.Factory(Glide.get(context), model)
            }

            model is Uri && (model.scheme == "http" || model.scheme == "https") -> {
                GlideHttpImageSource.Factory(Glide.get(context), model.toString())
            }

            model is String && model.startsWith("content://") -> {
                ImageSource.fromContent(context, Uri.parse(model)).toFactory()
            }

            model is Uri && model.scheme == "content" -> {
                ImageSource.fromContent(context, model).toFactory()
            }

            model is String && model.startsWith("file:///android_asset/") -> {
                val assetFileName = Uri.parse(model).pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is Uri && model.scheme == "file" && model.pathSegments.firstOrNull() == "android_asset" -> {
                val assetFileName = model.pathSegments
                    .takeIf { it.size > 1 }
                    ?.let { it.subList(1, it.size) }
                    ?.joinToString(separator = "/")
                assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
            }

            model is String && model.startsWith("/") -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is String && model.startsWith("file://") -> {
                val filePath = Uri.parse(model).path
                filePath?.let { ImageSource.fromFile(File(filePath)).toFactory() }
            }

            model is Uri && model.scheme == "file" -> {
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