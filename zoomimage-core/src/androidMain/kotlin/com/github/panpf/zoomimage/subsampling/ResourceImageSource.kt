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
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import okio.Source
import okio.source

/**
 * Create an image source from a resource id.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.ResourceImageSourceTest.testFromResource
 */
fun ImageSource.Companion.fromResource(
    resources: Resources,
    @RawRes @DrawableRes resId: Int
): ResourceImageSource {
    return ResourceImageSource(resources, resId)
}

/**
 * Create an image source from a resource id.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.ResourceImageSourceTest.testFromResource
 */
fun ImageSource.Companion.fromResource(
    context: Context,
    @RawRes @DrawableRes resId: Int
): ResourceImageSource {
    return ResourceImageSource(context, resId)
}

/**
 * Image source from resource
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.ResourceImageSourceTest
 */
class ResourceImageSource(
    val resources: Resources,
    @RawRes @DrawableRes val resId: Int
) : ImageSource {

    constructor(
        context: Context,
        @RawRes @DrawableRes drawableId: Int
    ) : this(context.resources, drawableId)

    override val key: String = "android.resources:///$resId"

    override fun openSource(): Source {
        return resources.openRawResource(resId).source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ResourceImageSource
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