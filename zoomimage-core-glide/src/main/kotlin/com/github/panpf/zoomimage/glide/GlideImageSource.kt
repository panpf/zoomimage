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
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromResource
import java.io.File
import java.net.URL


fun newGlideImageSource(context: Context, model: Any?): ImageSource? {
    return when {
        model is GlideUrl -> {
            GlideHttpImageSource(Glide.get(context), model)
        }

        model is URL -> {
            GlideHttpImageSource(Glide.get(context), GlideUrl(model))
        }

        model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
            GlideHttpImageSource(Glide.get(context), model)
        }

        model is String && model.startsWith("content://") -> {
            ImageSource.fromContent(context, Uri.parse(model))
        }

        model is Uri && model.scheme == "content" -> {
            ImageSource.fromContent(context, model)
        }

        model is String && model.startsWith("file:///android_asset/") -> {
            val assetFileName = Uri.parse(model).path.orEmpty()
            ImageSource.fromAsset(context, assetFileName)
        }

        model is Uri && model.scheme == "file" && model.authority == "android_asset" -> {
            val assetFileName = model.path.orEmpty()
            ImageSource.fromAsset(context, assetFileName)
        }

        model is File -> {
            ImageSource.fromFile(model)
        }

        model is Int -> {
            ImageSource.fromResource(context, model)
        }

        model is ByteArray -> {
            ImageSource.fromByteArray(model)
        }

        else -> {
            null
        }
    }
}