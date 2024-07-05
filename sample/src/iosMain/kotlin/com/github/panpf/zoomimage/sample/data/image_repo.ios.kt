package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.ImageFile
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext

actual suspend fun builtinImages(context: PlatformContext): List<ImageFile> {
    return listOf(
        ResourceImages.cat,
        ResourceImages.dog,
        ResourceImages.longEnd,
        ResourceImages.longWhale,
        ResourceImages.anim,
        ComposeResourceImages.hugeChina,
        ResourceImages.hugeCard,
        ResourceImages.hugeLongQmsht,
        HttpImages.hugeLongComic,
    ).plus(ResourceImages.exifs)
}

actual suspend fun localImages(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String> = emptyList()   // TODO Read ios photo album