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

    val hugeCard: ResourceImageFile =
        ResourceImageFile(
            "huge_card.jpg",
            "CARD",
            IntSizeCompat(7557, 5669),
            ExifOrientation.NORMAL
        )
    val hugeChina: ResourceImageFile =
        ResourceImageFile("huge_china.jpg", "CHINA", IntSizeCompat(6799, 4882))

    val hugeLongComic: ResourceImageFile =
        ResourceImageFile("huge_long_comic.jpg", "COMIC", IntSizeCompat(690, 12176))
    val hugeLongQmsht: ResourceImageFile =
        ResourceImageFile("huge_long_qmsht.jpg", "QMSHT", IntSizeCompat(30000, 926))

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