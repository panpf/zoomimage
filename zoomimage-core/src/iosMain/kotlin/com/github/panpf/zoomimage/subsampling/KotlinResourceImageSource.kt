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

import com.github.panpf.zoomimage.util.defaultFileSystem
import okio.Path.Companion.toPath
import okio.Source
import platform.Foundation.NSBundle

/**
 * Create an image source from a kotlin resource path.
 */
fun ImageSource.Companion.fromKotlinResource(
    resourcePath: String,
): KotlinResourceImageSource {
    return KotlinResourceImageSource(resourcePath)
}

class KotlinResourceImageSource(val resourcePath: String) : ImageSource {

    override val key: String = resourcePath

    override fun openSource(): Source {
        val resourcePath = NSBundle.mainBundle.resourcePath!!.toPath()
        val filePath = resourcePath.resolve("compose-resources").resolve(resourcePath)
        return defaultFileSystem().source(filePath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KotlinResourceImageSource) return false
        if (resourcePath != other.resourcePath) return false
        return true
    }

    override fun hashCode(): Int {
        return resourcePath.hashCode()
    }

    override fun toString(): String {
        return "KotlinResourceImageSource($resourcePath)"
    }

//    class Factory(val resourcePath: String) : ImageSource.Factory {
//
//        override val key: String = resourcePath
//
//        override suspend fun create(): KotlinResourceImageSource {
//            return KotlinResourceImageSource(resourcePath)
//        }
//
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (other !is Factory) return false
//            if (resourcePath != other.resourcePath) return false
//            return true
//        }
//
//        override fun hashCode(): Int {
//            return resourcePath.hashCode()
//        }
//
//        override fun toString(): String {
//            return "KotlinResourceImageSource.Factory($resourcePath)"
//        }
//    }
}