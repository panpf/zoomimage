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

package com.github.panpf.zoomimage.subsampling

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * Create an image source from a content URI.
 */
fun ImageSource.Companion.fromContent(context: Context, uri: Uri): ContentImageSource {
    return ContentImageSource(context, uri)
}

/**
 * Create an image source from a resource id.
 */
fun ImageSource.Companion.fromResource(
    resources: Resources,
    @RawRes @DrawableRes resId: Int
): ResourceImageSource {
    return ResourceImageSource(resources, resId)
}

/**
 * Create an image source from a resource id.
 */
fun ImageSource.Companion.fromResource(
    context: Context,
    @RawRes @DrawableRes resId: Int
): ResourceImageSource {
    return ResourceImageSource(context, resId)
}

/**
 * Create an image source from an asset file name.
 */
fun ImageSource.Companion.fromAsset(context: Context, assetFileName: String): AssetImageSource {
    return AssetImageSource(context, assetFileName)
}


class AssetImageSource(val context: Context, val assetFileName: String) : ImageSource {

    override val key: String = "asset://$assetFileName"

    override fun openInputStream(): Result<InputStream> = kotlin.runCatching {
        context.assets.open(assetFileName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AssetImageSource) return false
        if (context != other.context) return false
        if (assetFileName != other.assetFileName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + assetFileName.hashCode()
        return result
    }

    override fun toString(): String {
        return "AssetImageSource('$assetFileName')"
    }
}

class ContentImageSource(val context: Context, val uri: Uri) : ImageSource {

    override val key: String = uri.toString()

    override fun openInputStream(): Result<InputStream> = kotlin.runCatching {
        context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("Unable to open stream. uri='$uri'")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentImageSource) return false
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

class ResourceImageSource(
    val resources: Resources,
    @RawRes @DrawableRes val resId: Int
) : ImageSource {

    constructor(
        context: Context,
        @RawRes @DrawableRes drawableId: Int
    ) : this(context.resources, drawableId)

    override val key: String = "android.resources://resource?resId=$resId"

    override fun openInputStream(): Result<InputStream> = kotlin.runCatching {
        resources.openRawResource(resId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceImageSource) return false
        if (resources != other.resources) return false
        if (resId != other.resId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = resources.hashCode()
        result = 31 * result + resId
        return result
    }

    override fun toString(): String {
        return "ResourceImageSource($resId)"
    }
}