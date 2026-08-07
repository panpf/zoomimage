package com.github.panpf.zoomimage.sample.data

import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.images.AndroidResourceImageFiles
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.images.ContentImageFiles
import com.github.panpf.zoomimage.images.HttpImageFiles
import com.github.panpf.zoomimage.images.LocalImageFiles

actual suspend fun buildPlatformBuiltinPhotoList(sketch: Sketch): List<String> {
    // TODO Use multiple ImageFiles so you can directly test various Fetchers on the Local page.
    return listOf(
        ContentImageFiles.with(sketch.context).cat,
        ComposeResImageFiles.dog,
        ComposeResImageFiles.giraffe,
        ComposeResImageFiles.horse,
        ComposeResImageFiles.anim,
        ComposeResImageFiles.longEnd,
        ComposeResImageFiles.longWhale,
        ComposeResImageFiles.hugeChina,
        AndroidResourceImageFiles.hugeCard,
        LocalImageFiles.with(sketch.context).hugeLongQmsht,
        HttpImageFiles.hugeLongComic,
    ).plus(ComposeResImageFiles.exifs)
        .map { it.uri }
}