package com.githb.panpf.zoomimage.images

import android.os.Environment

object LocalImages {

    private val path =
        "file://${Environment.getExternalStorageDirectory()}/Android/data/com.github.panpf.zoomimage.sample/files/assets/"

    val cat = ResourceImages.cat.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val dog = ResourceImages.dog.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val anim = ResourceImages.anim.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longEnd = ResourceImages.longEnd.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longWhale = ResourceImages.longWhale.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeChina = ResourceImages.hugeChina.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeCard = ResourceImages.hugeCard.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongQmsht =
        ResourceImages.hugeLongQmsht.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongComic =
        ResourceImages.hugeLongComic.let { it.copy(uri = it.uri.replace("asset://", path)) }

    val exifFlipHorizontal =
        ResourceImages.exifFlipHorizontal.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifFlipVertical =
        ResourceImages.exifFlipVertical.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifNormal =
        ResourceImages.exifNormal.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifRotate90 =
        ResourceImages.exifRotate90.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifRotate180 =
        ResourceImages.exifRotate180.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifRotate270 =
        ResourceImages.exifRotate270.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifTranspose =
        ResourceImages.exifTranspose.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val exifTransverse =
        ResourceImages.exifTransverse.let { it.copy(uri = it.uri.replace("asset://", path)) }

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