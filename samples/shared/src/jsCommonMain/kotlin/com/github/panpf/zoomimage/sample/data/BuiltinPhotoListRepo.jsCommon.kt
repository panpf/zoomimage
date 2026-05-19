package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.ComposeResImageFiles
import com.githb.panpf.zoomimage.images.HttpImageFiles
import com.github.panpf.sketch.Sketch

actual suspend fun buildPlatformBuiltinPhotoList(sketch: Sketch): List<String> {
    return listOf(
        ComposeResImageFiles.cat,
        ComposeResImageFiles.dog,
        ComposeResImageFiles.longEnd,
        ComposeResImageFiles.longWhale,
        ComposeResImageFiles.anim,
        ComposeResImageFiles.hugeChina,
        ComposeResImageFiles.hugeCard,
        ComposeResImageFiles.hugeLongQmsht,
        HttpImageFiles.hugeLongComic,
    ).plus(ComposeResImageFiles.exifs)
        .map { it.uri }
}