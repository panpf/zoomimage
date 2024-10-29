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

import com.github.panpf.zoomimage.annotation.WorkerThread
import okio.Source

/**
 * Image source for subsampling.
 */
interface ImageSource {

    /**
     * Unique key for this image source.
     */
    val key: String

    /**
     * Open an input stream for the image.
     */
    @WorkerThread
    fun openSource(): Source

    interface Factory {

        /**
         * Unique key for this image source.
         */
        val key: String

        @WorkerThread
        suspend fun create(): ImageSource
    }

    companion object

    /**
     * Wrapper factory
     *
     * @see com.github.panpf.zoomimage.core.common.test.subsampling.ImageSourceTest
     */
    class WrapperFactory(val imageSource: ImageSource) : Factory {

        override val key: String = imageSource.key

        override suspend fun create(): ImageSource = imageSource

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as WrapperFactory
            return imageSource == other.imageSource
        }

        override fun hashCode(): Int {
            return imageSource.hashCode()
        }

        override fun toString(): String {
            return "WrapperFactory($imageSource)"
        }
    }
}

/**
 * Convert [ImageSource] to [ImageSource.Factory]
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.ImageSourceTest.testToFactory
 */
fun ImageSource.toFactory(): ImageSource.Factory = ImageSource.WrapperFactory(this)