/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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

import com.github.panpf.sketch.decode.internal.ExifOrientationHelper
import com.github.panpf.sketch.util.Size

object ResourceImages {

    val cat: ResourceImageFile = ResourceImageFile("cat.jpg", "CAT", Size(1100, 1650))
    val dog: ResourceImageFile = ResourceImageFile("dog.jpg", "DOG", Size(1100, 733))

    val anim: ResourceImageFile = ResourceImageFile("anim.gif", "GIF", Size(480, 480))

    val longEnd: ResourceImageFile = ResourceImageFile("long_end.jpg", "END", Size(2000, 618))
    val longWhale: ResourceImageFile = ResourceImageFile("long_whale.jpg", "WHALE", Size(672, 1916))

    val hugeCard: ResourceImageFile = ResourceImageFile("huge_card.jpg", "CARD", Size(7557, 5669))
    val hugeChina: ResourceImageFile =
        ResourceImageFile("huge_china.jpg", "CHINA", Size(6799, 4882))

    val hugeLongComic: ResourceImageFile =
        ResourceImageFile("huge_long_comic.jpg", "COMIC", Size(690, 12176))
    val hugeLongQmsht: ResourceImageFile =
        ResourceImageFile("huge_long_qmsht.jpg", "QMSHT", Size(30000, 926))

    val exifFlipHorizontal: ResourceImageFile = ResourceImageFile(
        "exif_flip_horizontal.jpg",
        "FLIP_HOR",
        Size(6400, 1080),
        ExifOrientationHelper.FLIP_HORIZONTAL
    )
    val exifFlipVertical: ResourceImageFile = ResourceImageFile(
        "exif_flip_vertical.jpg",
        "FLIP_VER",
        Size(6400, 1080),
        ExifOrientationHelper.FLIP_VERTICAL
    )
    val exifNormal: ResourceImageFile = ResourceImageFile(
        "exif_normal.jpg",
        "NORMAL",
        Size(6400, 1080),
        ExifOrientationHelper.NORMAL
    )
    val exifRotate90: ResourceImageFile = ResourceImageFile(
        "exif_rotate_90.jpg",
        "ROTATE_90",
        Size(1080, 6400),
        ExifOrientationHelper.ROTATE_90
    )
    val exifRotate180: ResourceImageFile = ResourceImageFile(
        "exif_rotate_180.jpg",
        "ROTATE_180",
        Size(6400, 1080),
        ExifOrientationHelper.ROTATE_180
    )
    val exifRotate270: ResourceImageFile = ResourceImageFile(
        "exif_rotate_270.jpg",
        "ROTATE_270",
        Size(1080, 6400),
        ExifOrientationHelper.ROTATE_270
    )
    val exifTranspose: ResourceImageFile = ResourceImageFile(
        "exif_transpose.jpg",
        "TRANSPOSE",
        Size(1080, 6400),
        ExifOrientationHelper.TRANSPOSE
    )
    val exifTransverse: ResourceImageFile = ResourceImageFile(
        "exif_transverse.jpg",
        "TRANSVERSE",
        Size(1080, 6400),
        ExifOrientationHelper.TRANSVERSE
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