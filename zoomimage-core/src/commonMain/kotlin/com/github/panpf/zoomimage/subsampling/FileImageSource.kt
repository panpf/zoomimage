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

import com.github.panpf.zoomimage.util.defaultFileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Source

/**
 * Create an image source from a path.
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.FileImageSourceTest.testFromFile
 */
fun ImageSource.Companion.fromFile(path: Path): FileImageSource {
    return FileImageSource(path)
}

/**
 * Create an image source from a file path.
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.FileImageSourceTest.testFromFile
 */
fun ImageSource.Companion.fromFile(path: String): FileImageSource {
    return FileImageSource(path.toPath())
}

/**
 * Image source from a file path.
 *
 * @see com.github.panpf.zoomimage.core.desktop.test.subsampling.FileImageSourceTest
 */
class FileImageSource(val path: Path) : ImageSource {

    override val key: String = "file://$path"

    override fun openSource(): Source {
        return defaultFileSystem().source(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as FileImageSource
        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return "FileImageSource('$path')"
    }
}