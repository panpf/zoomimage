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

import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

object ComposeResImageFiles {

    val cat: ComposeResImageFile = ComposeResImageFile(
        name = "cat.jpg",
        mimeType = "image/jpeg",
        length = 133296L,
        size = IntSizeCompat(width = 1100, height = 1650)
    )
    val dog: ComposeResImageFile = ComposeResImageFile(
        name = "dog.jpg",
        mimeType = "image/jpeg",
        length = 115239L,
        size = IntSizeCompat(width = 1100, height = 733)
    )
    val giraffe: ComposeResImageFile = ComposeResImageFile(
        name = "giraffe.heic",
        mimeType = "image/heif",
        length = 1106094L,
        size = IntSizeCompat(width = 3840, height = 2160)
    )
    val horse: ComposeResImageFile = ComposeResImageFile(
        name = "horse.avif",
        mimeType = "image/avif",
        length = 606462L,
        size = IntSizeCompat(width = 3840, height = 2400)
    )

    val anim: ComposeResImageFile = ComposeResImageFile(
        name = "anim.gif",
        mimeType = "image/gif",
        length = 76938L,
        animated = true,
        size = IntSizeCompat(width = 480, height = 480)
    )

    val longEnd: ComposeResImageFile = ComposeResImageFile(
        name = "long_end.jpg",
        mimeType = "image/jpeg",
        length = 187404L,
        size = IntSizeCompat(width = 2000, height = 618),
        exifOrientation = ExifOrientation.NORMAL
    )
    val longWhale: ComposeResImageFile = ComposeResImageFile(
        name = "long_whale.jpg",
        mimeType = "image/jpeg",
        length = 72566L,
        size = IntSizeCompat(width = 672, height = 1916)
    )

    val hugeCard: ComposeResImageFile = ComposeResImageFile(
        name = "huge_card.jpg",
        mimeType = "image/jpeg",
        length = 3966403L,
        size = IntSizeCompat(width = 7557, height = 5669),
        exifOrientation = ExifOrientation.NORMAL
    )
    val hugeCardThumbnail: ComposeResImageFile = ComposeResImageFile(
        name = "huge_card_thumbnail.jpg",
        mimeType = "image/jpeg",
        length = 65771L,
        size = IntSizeCompat(width = 500, height = 375),
        exifOrientation = ExifOrientation.NORMAL
    )
    val hugeChina: ComposeResImageFile = ComposeResImageFile(
        name = "huge_china.jpg",
        mimeType = "image/jpeg",
        length = 6717244L,
        size = IntSizeCompat(width = 6799, height = 4882)
    )
    val hugeChinaThumbnail: ComposeResImageFile = ComposeResImageFile(
        name = "huge_china_thumbnail.jpg",
        mimeType = "image/jpeg",
        length = 76881L,
        size = IntSizeCompat(width = 500, height = 359)
    )
    val hugeLongComic: ComposeResImageFile = ComposeResImageFile(
        name = "huge_long_comic.jpg",
        mimeType = "image/jpeg",
        length = 1437197L,
        size = IntSizeCompat(width = 690, height = 12176)
    )
    val hugeLongComicThumbnail: ComposeResImageFile = ComposeResImageFile(
        name = "huge_long_comic_thumbnail.jpg",
        mimeType = "image/jpeg",
        length = 35411L,
        size = IntSizeCompat(width = 57, height = 1000)
    )
    val hugeLongQmsht: ComposeResImageFile = ComposeResImageFile(
        name = "huge_long_qmsht.jpg",
        mimeType = "image/jpeg",
        length = 8063397L,
        size = IntSizeCompat(30000, 926)
    )
    val hugeLongQmshtThumbnail: ComposeResImageFile = ComposeResImageFile(
        name = "huge_long_qmsht_thumbnail.jpg",
        mimeType = "image/jpeg",
        length = 21729L,
        size = IntSizeCompat(width = 1000, height = 31)
    )

    val exifFlipHorizontal: ComposeResImageFile = ComposeResImageFile(
        name = "exif_flip_horizontal.jpg",
        mimeType = "image/jpeg",
        length = 1000358L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.FLIP_HORIZONTAL
    )
    val exifFlipVertical: ComposeResImageFile = ComposeResImageFile(
        name = "exif_flip_vertical.jpg",
        mimeType = "image/jpeg",
        length = 1000273L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.FLIP_VERTICAL
    )
    val exifNormal: ComposeResImageFile = ComposeResImageFile(
        name = "exif_normal.jpg",
        mimeType = "image/jpeg",
        length = 1000409L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.NORMAL
    )
    val exifRotate90: ComposeResImageFile = ComposeResImageFile(
        name = "exif_rotate_90.jpg",
        mimeType = "image/jpeg",
        length = 1004742L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.ROTATE_90
    )
    val exifRotate180: ComposeResImageFile = ComposeResImageFile(
        name = "exif_rotate_180.jpg",
        mimeType = "image/jpeg",
        length = 1000902L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.ROTATE_180
    )
    val exifRotate270: ComposeResImageFile = ComposeResImageFile(
        name = "exif_rotate_270.jpg",
        mimeType = "image/jpeg",
        length = 1005275L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.ROTATE_270
    )
    val exifTranspose: ComposeResImageFile = ComposeResImageFile(
        name = "exif_transpose.jpg",
        mimeType = "image/jpeg",
        length = 1005221L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.TRANSPOSE
    )
    val exifTransverse: ComposeResImageFile = ComposeResImageFile(
        name = "exif_transverse.jpg",
        mimeType = "image/jpeg",
        length = 1005601L,
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.TRANSVERSE
    )
    val exifs = arrayOf(
        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )

    val woodpile: ComposeResImageFile = ComposeResImageFile(
        name = "woodpile.jpg",
        mimeType = "image/jpeg",
        length = 2596250L,
        size = IntSizeCompat(6010, 4000),
        exifOrientation = ExifOrientation.UNDEFINED
    )

    val values: Array<ComposeResImageFile> = arrayOf(
        cat,
        dog,
        giraffe,
        horse,

        anim,

        longEnd,
        longWhale,

        hugeCard,
        hugeChina,

        hugeLongComic,
        hugeLongQmsht,

        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )
}

class ComposeResImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED
) : ImageFile {

    override val uri = newComposeResourceUri(Res.getUri("files/$name"))

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    @OptIn(InternalResourceApi::class)
    suspend fun toImageSource(): ImageSource {
        val uri = Res.getUri("files/$name")
        val index = uri.indexOf("composeResources/")
        require(index != -1) {
            "The uri of the resource is invalid: $uri"
        }
        val path = uri.substring(index)
        val bytes = readResourceBytes(path)
        return ByteArrayImageSource(byteArray = bytes)
    }

    override fun toString(): String =
        "ComposeResImageFile(name='$name', size=$size, mimeType=$mimeType, exifOrientation=$exifOrientation)"
}

private fun newComposeResourceUri(resourcePath: String): String {
    if (resourcePath.startsWith("composeResources/")) {
        return "file:///compose_resource/$resourcePath"
    }

    val index = resourcePath.indexOf("/composeResources/")
    if (index != -1) {
        val realResourcePath = resourcePath.substring(index + 1)
        return "file:///compose_resource/$realResourcePath"
    }

    throw IllegalArgumentException("Unsupported compose resource path: $resourcePath")
}