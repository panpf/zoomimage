package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.ComposeResImageFiles
import com.githb.panpf.zoomimage.images.HttpImageFiles
import com.githb.panpf.zoomimage.images.ImageFile
import com.github.panpf.sketch.PlatformContext


actual suspend fun builtinImages(context: PlatformContext): List<ImageFile> {
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
}

actual suspend fun localImages(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String> = emptyList()