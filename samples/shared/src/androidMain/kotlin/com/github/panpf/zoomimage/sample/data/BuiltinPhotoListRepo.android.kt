package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.AndroidLocalImageFiles
import com.githb.panpf.zoomimage.images.AndroidResourceImageFiles
import com.githb.panpf.zoomimage.images.ComposeResImageFiles
import com.githb.panpf.zoomimage.images.ContentImageFiles
import com.githb.panpf.zoomimage.images.HttpImageFiles
import com.github.panpf.sketch.Sketch

actual suspend fun buildPlatformBuiltinPhotoList(sketch: Sketch): List<String> {
    return listOf(
        ComposeResImageFiles.cat,
        ComposeResImageFiles.dog,
        ComposeResImageFiles.anim,
        ComposeResImageFiles.longEnd,
        ContentImageFiles.create(sketch.context).longWhale,
        ComposeResImageFiles.hugeChina,
        AndroidResourceImageFiles.hugeCard,
        AndroidLocalImageFiles.with(sketch.context).hugeLongQmsht,
        HttpImageFiles.hugeLongComic,
    ).plus(ComposeResImageFiles.exifs)
        .map { it.uri }
}