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

import com.github.panpf.zoomimage.annotation.WorkerThread
import okio.Path
import okio.Path.Companion.toPath
import okio.Source

/**
 * Image source for subsampling.
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.ImageSourceTest]
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
    suspend fun openSource(): Result<Source>

    companion object {

        /**
         * Create an image source from a path.
         */
        fun fromFile(path: Path): FileImageSource {
            return FileImageSource(path)
        }

        /**
         * Create an image source from a file path.
         */
        fun fromFile(path: String): FileImageSource {
            return FileImageSource(path.toPath())
        }

        /**
         * Create an image source from a ByteArray.
         */
        fun fromByteArray(byteArray: ByteArray): ByteArrayImageSource {
            return ByteArrayImageSource(byteArray)
        }
    }
}