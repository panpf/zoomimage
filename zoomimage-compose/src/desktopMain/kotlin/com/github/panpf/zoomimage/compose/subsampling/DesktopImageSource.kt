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

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ResourceLoader
import com.github.panpf.zoomimage.subsampling.ImageSource
import java.io.InputStream

/**
 * Create an image source from a resource id.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun ImageSource.Companion.fromResource(
    resourcePath: String,
    resourcesLoader: ResourceLoader = ResourceLoader.Default,
): ResourceImageSource {
    return ResourceImageSource(resourcePath, resourcesLoader)
}

@OptIn(ExperimentalComposeUiApi::class)
class ResourceImageSource(
    val resourcePath: String,
    val resourcesLoader: ResourceLoader = ResourceLoader.Default,
) : ImageSource {

    override val key: String = resourcePath

    override fun openInputStream(): Result<InputStream> = kotlin.runCatching {
        return Result.success(resourcesLoader.load(resourcePath))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceImageSource) return false
        if (resourcesLoader != other.resourcesLoader) return false
        if (resourcePath != other.resourcePath) return false
        return true
    }

    override fun hashCode(): Int {
        var result = resourcesLoader.hashCode()
        result = 31 * result + resourcePath.hashCode()
        return result
    }

    override fun toString(): String {
        return "ResourceImageSource($resourcePath)"
    }
}