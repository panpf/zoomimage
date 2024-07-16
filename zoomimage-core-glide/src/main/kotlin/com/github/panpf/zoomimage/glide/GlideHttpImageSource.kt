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

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.getDiskCache
import com.bumptech.glide.load.model.GlideUrl
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Source
import okio.source
import java.io.FileInputStream
import java.io.FileNotFoundException


class GlideHttpImageSource(
    val glide: Glide,
    val glideUrl: GlideUrl
) : ImageSource {

    constructor(glide: Glide, imageUri: String) : this(glide, GlideUrl(imageUri))

    override val key: String = glideUrl.cacheKey

    override fun openSource(): Source {
        // TODO support download
        val diskCache =
            getDiskCache(glide) ?: throw IllegalStateException("DiskCache is null")
        val file = diskCache.get(glideUrl)
            ?: throw FileNotFoundException("Cache file is null")
        return FileInputStream(file).source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GlideHttpImageSource) return false
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

//    class Factory(
//        val glide: Glide,
//        val glideUrl: GlideUrl
//    ) : ImageSource.Factory {
//
//        override val key: String = glideUrl.cacheKey
//
//        constructor(glide: Glide, imageUri: String) : this(glide, GlideUrl(imageUri))
//
//        override suspend fun create(): GlideHttpImageSource {
//            return GlideHttpImageSource(glide, glideUrl)
//        }
//
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is Factory) return false
//            if (glide != other.glide) return false
//            if (glideUrl != other.glideUrl) return false
//            return true
//        }
//
//        override fun hashCode(): Int {
//            var result = glide.hashCode()
//            result = 31 * result + glideUrl.hashCode()
//            return result
//        }
//
//        override fun toString(): String {
//            return "GlideHttpImageSource.Factory('$glideUrl')"
//        }
//    }
}