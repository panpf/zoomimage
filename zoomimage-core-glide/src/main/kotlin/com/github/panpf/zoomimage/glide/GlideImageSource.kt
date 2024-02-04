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
import androidx.annotation.WorkerThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.getDiskCache
import com.bumptech.glide.load.model.GlideUrl
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromResource
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
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

        model is Uri && (model.scheme == "http" || model.scheme == "https") -> {
            GlideHttpImageSource(Glide.get(context), model.toString())
        }

        model is String && model.startsWith("content://") -> {
            ImageSource.fromContent(context, Uri.parse(model))
        }

        model is Uri && model.scheme == "content" -> {
            ImageSource.fromContent(context, model)
        }

        model is String && model.startsWith("file:///android_asset/") -> {
            val assetFileName = Uri.parse(model).pathSegments
                .takeIf { it.size > 1 }
                ?.let { it.subList(1, it.size) }
                ?.joinToString(separator = "/")
            assetFileName?.let { ImageSource.fromAsset(context, it) }
        }

        model is Uri && model.scheme == "file" && model.pathSegments.firstOrNull() == "android_asset" -> {
            val assetFileName = model.pathSegments
                .takeIf { it.size > 1 }
                ?.let { it.subList(1, it.size) }
                ?.joinToString(separator = "/")
            assetFileName?.let { ImageSource.fromAsset(context, it) }
        }

        model is String && model.startsWith("file://") -> {
            val filePath = Uri.parse(model).path
            filePath?.let { ImageSource.fromFile(File(filePath)) }
        }

        model is Uri && model.scheme == "file" -> {
            val filePath = model.path
            filePath?.let { ImageSource.fromFile(File(filePath)) }
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

class GlideHttpImageSource(
    private val glide: Glide,
    private val glideUrl: GlideUrl
) : ImageSource {

    constructor(glide: Glide, imageUri: String) : this(glide, GlideUrl(imageUri))

    override val key: String = glideUrl.cacheKey

    @WorkerThread
    override fun openInputStream(): Result<InputStream> = kotlin.runCatching {
        val diskCache =
            getDiskCache(glide) ?: throw IllegalStateException("DiskCache is null")
        val file = diskCache.get(glideUrl)
            ?: throw FileNotFoundException("Cache file is null")
        FileInputStream(file)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GlideHttpImageSource
        if (glide != other.glide) return false
        if (glideUrl != other.glideUrl) return false
        return true
    }

    override fun hashCode(): Int {
        var result = glide.hashCode()
        result = 31 * result + glideUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "GlideHttpImageSource('$glideUrl')"
    }
}