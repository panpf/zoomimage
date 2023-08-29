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

package com.github.panpf.zoomimage.glide.internal

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.subsampling.ImageSource
import java.io.File


fun newGlideImageSource(context: Context, model: Any?): ImageSource? {
    return when {
        model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
            GlideHttpImageSource(Glide.get(context), model)
        }

        model is String && model.startsWith("content://") -> {
            ImageSource.fromContent(context, Uri.parse(model))
        }

        model is String && model.startsWith("file:///android_asset/") -> {
            val assetFileName = model.replace("file:///android_asset/", "")
            ImageSource.fromAsset(context, assetFileName)
        }

        model is String && model.startsWith("file://") -> {
            ImageSource.fromFile(File(model.replace("file://", "")))
        }

        model is Int -> {
            ImageSource.fromResource(context, model)
        }

        else -> {
            null
        }
    }
}