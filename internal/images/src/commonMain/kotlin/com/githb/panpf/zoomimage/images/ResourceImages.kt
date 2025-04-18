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

    val cat: ResourceImageFile = ResourceImageFile("cat.jpg", "CAT", IntSizeCompat(1100, 1650))
    val dog: ResourceImageFile = ResourceImageFile("dog.jpg", "DOG", IntSizeCompat(1100, 733))

    val anim: ResourceImageFile = ResourceImageFile("anim.gif", "GIF", IntSizeCompat(480, 480))

    val longEnd: ResourceImageFile =
        ResourceImageFile("long_end.jpg", "END", IntSizeCompat(2000, 618), ExifOrientation.NORMAL)
    val longWhale: ResourceImageFile =
        ResourceImageFile("long_whale.jpg", "WHALE", IntSizeCompat(672, 1916))

    val hugeCard: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_card.jpg",
        name = "CARD",
        size = IntSizeCompat(7557, 5669),
        exifOrientation = ExifOrientation.NORMAL
    )
    val hugeCardThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_card_thumbnail.jpg",
        name = "CARD_T",
        size = IntSizeCompat(500, 375),
        exifOrientation = ExifOrientation.NORMAL
    )
    val hugeChina: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_china.jpg",
        name = "CHINA",
        size = IntSizeCompat(6799, 4882)
    )
    val hugeChinaThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_china_thumbnail.jpg",
        name = "CHINA_T",
        size = IntSizeCompat(500, 359)
    )
    val hugeLongComic: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_comic.jpg",
        name = "COMIC",
        size = IntSizeCompat(690, 12176)
    )
    val hugeLongComicThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_comic_thumbnail.jpg",
        name = "COMIC_T",
        size = IntSizeCompat(57, 1000)
    )
    val hugeLongQmsht: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_qmsht.jpg",
        name = "QMSHT",
        size = IntSizeCompat(30000, 926)
    )
    val hugeLongQmshtThumbnail: ResourceImageFile = ResourceImageFile(
        resourceName = "huge_long_qmsht_thumbnail.jpg",
        name = "QMSHT_T",
        size = IntSizeCompat(1000, 31)
    )

    val exifFlipHorizontal: ResourceImageFile = ResourceImageFile(
        "exif_flip_horizontal.jpg",
        "FLIP_HOR",
        IntSizeCompat(1080, 6400),
        ExifOrientation.FLIP_HORIZONTAL
    )
    val exifFlipVertical: ResourceImageFile = ResourceImageFile(
        "exif_flip_vertical.jpg",
        "FLIP_VER",
        IntSizeCompat(1080, 6400),
        ExifOrientation.FLIP_VERTICAL
    )
    val exifNormal: ResourceImageFile = ResourceImageFile(
        "exif_normal.jpg",
        "NORMAL",
        IntSizeCompat(1080, 6400),
        ExifOrientation.NORMAL
    )
    val exifRotate90: ResourceImageFile = ResourceImageFile(
        "exif_rotate_90.jpg",
        "ROTATE_90",
        IntSizeCompat(1080, 6400),
        ExifOrientation.ROTATE_90
    )
    val exifRotate180: ResourceImageFile = ResourceImageFile(
        "exif_rotate_180.jpg",
        "ROTATE_180",
        IntSizeCompat(1080, 6400),
        ExifOrientation.ROTATE_180
    )
    val exifRotate270: ResourceImageFile = ResourceImageFile(
        "exif_rotate_270.jpg",
        "ROTATE_270",
        IntSizeCompat(1080, 6400),
        ExifOrientation.ROTATE_270
    )
    val exifTranspose: ResourceImageFile = ResourceImageFile(
        "exif_transpose.jpg",
        "TRANSPOSE",
        IntSizeCompat(1080, 6400),
        ExifOrientation.TRANSPOSE
    )
    val exifTransverse: ResourceImageFile = ResourceImageFile(
        "exif_transverse.jpg",
        "TRANSVERSE",
        IntSizeCompat(1080, 6400),
        ExifOrientation.TRANSVERSE
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