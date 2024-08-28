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

package com.github.panpf.zoomimage.subsampling

import android.content.Context
import android.net.Uri
import okio.Source
import okio.source
import java.io.FileNotFoundException

/**
 * Create an image source from a content URI.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.ContentImageSourceTest.testFromContent
 */
fun ImageSource.Companion.fromContent(context: Context, uri: Uri): ContentImageSource {
    return ContentImageSource(context, uri)
}

/**
 * An image source that reads from a content URI.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.ContentImageSourceTest
 */
class ContentImageSource(val context: Context, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override fun openSource(): Source {
        return context.contentResolver.openInputStream(uri)?.source()
            ?: throw FileNotFoundException("Unable to open stream. uri='$uri'")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ContentImageSource
        if (context != other.context) return false
        if (uri != other.uri) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

    override fun toString(): String {
        return "ContentImageSource('$uri')"
    }
}