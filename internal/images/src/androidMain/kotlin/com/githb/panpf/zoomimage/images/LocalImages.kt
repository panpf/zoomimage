package com.githb.panpf.zoomimage.images

import android.os.Environment

object LocalImages {

    private val path =
        "file://${Environment.getExternalStorageDirectory()}/Android/data/com.github.panpf.zoomimage.sample/files/assets/"

    val cat = Images.cat.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val dog = Images.dog.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val anim = Images.anim.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longEnd = Images.longEnd.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longWhale = Images.longWhale.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeChina = Images.hugeChina.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeCard = Images.hugeCard.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongQmsht = Images.hugeLongQmsht.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongComic = Images.hugeLongComic.let { it.copy(uri = it.uri.replace("asset://", path)) }

    val exifFlipHorizontal =
        Images.exifFlipHorizontal.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifFlipVertical =
        Images.exifFlipVertical.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifNormal = Images.exifNormal.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifRotate90 = Images.exifRotate90.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifRotate180 = Images.exifRotate180.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifRotate270 = Images.exifRotate270.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifTranspose = Images.exifTranspose.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifTransverse =
        Images.exifTransverse.let { it.copy(uri = it.uri.replace("asset://", path)) }

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

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic,

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