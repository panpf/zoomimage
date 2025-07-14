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

package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat

object ResourceImages {

    val cat: ResourceImageFile = ResourceImageFile(
        resourceName = "cat.jpg",
        name = "CAT",
        size = IntSizeCompat(width = 1100, height = 1650)
    )
    val dog: ResourceImageFile = ResourceImageFile(
        resourceName = "dog.jpg",
        name = "DOG",
        size = IntSizeCompat(width = 1100, height = 733)
    )

    val anim: ResourceImageFile = ResourceImageFile(
        resourceName = "anim.gif",
        name = "GIF",
        size = IntSizeCompat(width = 480, height = 480)
    )

    val longEnd: ResourceImageFile = ResourceImageFile(
        resourceName = "long_end.jpg",
        name = "END",
        size = IntSizeCompat(width = 2000, height = 618),
        exifOrientation = ExifOrientation.NORMAL
    )
    val longWhale: ResourceImageFile = ResourceImageFile(
        resourceName = "long_whale.jpg",
        name = "WHALE",
        size = IntSizeCompat(width = 672, height = 1916)
    )

    val hugeCard: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_card.jpg",
        name = "CARD",
        size = IntSizeCompat(width = 7557, height = 5669),
        exifOrientation = ExifOrientation.NORMAL
    )
    val hugeCardThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_card_thumbnail.jpg",
        name = "CARD_T",
        size = IntSizeCompat(width = 500, height = 375),
        exifOrientation = ExifOrientation.NORMAL
    )
    val hugeChina: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_china.jpg",
        name = "CHINA",
        size = IntSizeCompat(width = 6799, height = 4882)
    )
    val hugeChinaThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_china_thumbnail.jpg",
        name = "CHINA_T",
        size = IntSizeCompat(width = 500, height = 359)
    )
    val hugeLongComic: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_comic.jpg",
        name = "COMIC",
        size = IntSizeCompat(width = 690, height = 12176)
    )
    val hugeLongComicThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_comic_thumbnail.jpg",
        name = "COMIC_T",
        size = IntSizeCompat(width = 57, height = 1000)
    )
    val hugeLongQmsht: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_qmsht.jpg",
        name = "QMSHT",
        size = IntSizeCompat(30000, 926)
    )
    val hugeLongQmshtThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_qmsht_thumbnail.jpg",
        name = "QMSHT_T",
        size = IntSizeCompat(width = 1000, height = 31)
    )

    val exifFlipHorizontal: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_flip_horizontal.jpg",
        name = "FLIP_HOR",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.FLIP_HORIZONTAL
    )
    val exifFlipVertical: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_flip_vertical.jpg",
        name = "FLIP_VER",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.FLIP_VERTICAL
    )
    val exifNormal: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_normal.jpg",
        name = "NORMAL",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.NORMAL
    )
    val exifRotate90: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_rotate_90.jpg",
        name = "ROTATE_90",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.ROTATE_90
    )
    val exifRotate180: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_rotate_180.jpg",
        name = "ROTATE_180",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.ROTATE_180
    )
    val exifRotate270: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_rotate_270.jpg",
        name = "ROTATE_270",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.ROTATE_270
    )
    val exifTranspose: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_transpose.jpg",
        name = "TRANSPOSE",
        size = IntSizeCompat(width = 1080, height = 6400),
        exifOrientation = ExifOrientation.TRANSPOSE
    )
    val exifTransverse: ResourceImageFile = ResourceImageFile(
        resourceName = "exif_transverse.jpg",
        name = "TRANSVERSE",
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

    val woodpile: ResourceImageFile = ResourceImageFile(
        resourceName = "woodpile.jpg",
        name = "WOODPILE",
        size = IntSizeCompat(6010, 4000),
        exifOrientation = ExifOrientation.UNDEFINED
    )

    val values: Array<ResourceImageFile> = arrayOf(
        cat,
        dog,

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