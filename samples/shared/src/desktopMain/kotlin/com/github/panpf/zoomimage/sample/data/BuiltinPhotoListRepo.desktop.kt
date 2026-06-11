package com.github.panpf.zoomimage.sample.data

import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.images.HttpImageFiles

actual suspend fun buildPlatformBuiltinPhotoList(sketch: Sketch): List<String> {
    return listOf(
        ComposeResImageFiles.cat,
        ComposeResImageFiles.dog,
        ComposeResImageFiles.giraffe,
        ComposeResImageFiles.horse,
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