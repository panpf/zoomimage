package com.githb.panpf.zoomimage.images

object ContentImages {

    private const val path =
        "content://com.github.panpf.zoomimage.sample.fileprovider/asset_images/"

    val cat = Images.cat.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val dog = Images.dog.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val anim = Images.anim.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longEnd = Images.longEnd.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longWhale = Images.longWhale.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeChina = Images.hugeChina.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeCard = Images.hugeCard.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongQmsht = Images.hugeLongQmsht.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongComic = Images.hugeLongComic.let { it.copy(uri = it.uri.replace("asset://", path)) }

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic
    )
}