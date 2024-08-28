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
import okio.Source
import okio.source

/**
 * Create an image source from an asset file name.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.AssetImageSourceTest.testFromAsset
 */
fun ImageSource.Companion.fromAsset(context: Context, assetFileName: String): AssetImageSource {
    return AssetImageSource(context, assetFileName)
}

/**
 * An image source created from an asset file name.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.AssetImageSourceTest
 */
class AssetImageSource(val context: Context, val assetFileName: String) : ImageSource {

    override val key: String = "file:///android_asset/$assetFileName"

    override fun openSource(): Source {
        return context.assets.open(assetFileName).source()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as AssetImageSource
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