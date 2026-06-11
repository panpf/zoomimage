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

package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.jetbrains.compose.resources.InternalResourceApi

object KotlinResImageFiles {
    val cat: KotlinResImageFile = ComposeResImageFiles.cat.toKotlinResImageFile()
    val dog: KotlinResImageFile = ComposeResImageFiles.dog.toKotlinResImageFile()
}

class KotlinResImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED
) : ImageFile {

    override val uri = "file:///kotlin_resource/$name"

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    @OptIn(InternalResourceApi::class)
    suspend fun toImageSource(): ImageSource {
        return ImageSource.fromKotlinResource(name)
    }

    override fun toString(): String =
        "KotlinResImageFile(name='$name', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toKotlinResImageFile(): KotlinResImageFile = KotlinResImageFile(
    name = this.name,
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)