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

import okio.Buffer
import okio.Source

/**
 * Create an image source from a ByteArray.
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.ByteArrayImageSourceTest.testFromByteArray
 */
fun ImageSource.Companion.fromByteArray(byteArray: ByteArray): ByteArrayImageSource {
    return ByteArrayImageSource(byteArray)
}

/**
 * Image source for subsampling from a ByteArray.
 *
 * @see com.github.panpf.zoomimage.core.common.test.subsampling.ByteArrayImageSourceTest
 */
class ByteArrayImageSource(val byteArray: ByteArray) : ImageSource {

    override val key: String = byteArray.toString()

    override fun openSource(): Source {
        return Buffer().write(byteArray)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ByteArrayImageSource
        return byteArray.contentEquals(other.byteArray)
    }

    override fun hashCode(): Int {
        return byteArray.hashCode()
    }

    override fun toString(): String {
        return "ByteArrayImageSource('$byteArray')"
    }
}