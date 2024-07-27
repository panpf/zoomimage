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
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.fromResource
import com.github.panpf.zoomimage.subsampling.toFactory
import com.squareup.picasso.Picasso
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * Convert picasso data to [ImageSource.Factory]
 */
interface PicassoDataToImageSource {
    fun dataToImageSource(data: Any): ImageSource.Factory?
}

/**
 * Default implementation of [PicassoDataToImageSource]
 *
 * @see com.github.panpf.zoomimage.core.picasso.test.PicassoDataToImageSourceImplTest
 */
class PicassoDataToImageSourceImpl(private val context: Context) : PicassoDataToImageSource {

    override fun dataToImageSource(data: Any): ImageSource.Factory? {
        if (data is Uri) {
            return when {
                data.scheme == "http" || data.scheme == "https" -> {
                    PicassoHttpImageSource(Picasso.get(), data).toFactory()
                }

                data.scheme == "content" -> {
                    ImageSource.fromContent(context, data).toFactory()
                }

                data.scheme == "file" && data.pathSegments.firstOrNull() == "android_asset" -> {
                    val assetFileName = data.pathSegments
                        .takeIf { it.size > 1 }
                        ?.let { it.subList(1, it.size) }
                        ?.joinToString(separator = "/")
                    assetFileName?.let { ImageSource.fromAsset(context, it).toFactory() }
                }

                data.scheme == "file" -> {
                    val filePath = data.path
                    filePath?.let { ImageSource.fromFile(File(filePath).toOkioPath()).toFactory() }
                }

                else -> {
                    null
                }
            }
        }

        if (data is Int && data != 0) {
            return ImageSource.fromResource(context, data).toFactory()
        }

        return null
    }
}