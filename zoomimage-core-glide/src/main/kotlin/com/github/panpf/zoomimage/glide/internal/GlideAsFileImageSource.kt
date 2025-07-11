/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Source
import okio.source

class GlideAsFileImageSource(val context: Context, val model: Any) : ImageSource {

    override val key: String = model.toString()

    override fun openSource(): Source {
        return Glide.with(context)
            .asFile()
            .load(model)
            .onlyRetrieveFromCache(true)
            .submit()
            .get()
            .source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GlideAsFileImageSource
        if (context != other.context) return false
        if (model != other.model) return false
        if (key != other.key) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + model.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }

    override fun toString(): String {
        return "GlideAsFileImageSource(model=$model)"
    }
}