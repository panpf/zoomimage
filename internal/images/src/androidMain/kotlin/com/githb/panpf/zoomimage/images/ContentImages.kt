package com.githb.panpf.zoomimage.images

object ContentImages {

    private const val path =
        "content://com.github.panpf.zoomimage.sample.fileprovider/asset_images/"

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