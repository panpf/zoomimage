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
import coil3.Uri
import coil3.toUri
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.toFactory
import okio.Path

/**
 * Convert coil model to [ImageSource.Factory] for js platform
 *
 * @see com.github.panpf.zoomimage.core.coil.jscommon.test.CoilModelToImageSourceImplTest
 */
actual class CoilModelToImageSourceImpl actual constructor(
    private val context: PlatformContext,
    private val imageLoader: ImageLoader
) : CoilModelToImageSource {

    actual override fun dataToImageSource(model: Any): ImageSource.Factory? {
        return when {
            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is Uri && (model.scheme == "http" || model.scheme == "https") -> {
                CoilHttpImageSource.Factory(context, imageLoader, model.toString())
            }

            model is String && model.startsWith("/") -> {
                ImageSource.fromFile(model).toFactory()
            }

            model is String && model.startsWith("file://") -> {
                val filePath = model.toUri().path
                filePath?.let { ImageSource.fromFile(filePath).toFactory() }
            }

            model is Uri && model.scheme == "file" -> {
                val filePath = model.path
                filePath?.let { ImageSource.fromFile(filePath).toFactory() }
            }

            model is Path -> {
                ImageSource.fromFile(model).toFactory()
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