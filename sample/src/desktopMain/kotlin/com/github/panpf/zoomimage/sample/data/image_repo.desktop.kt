package com.github.panpf.zoomimage.sample.data

import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.ImageFile
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import java.io.File
import java.util.Locale

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

val acceptImageExtensions = setOf("jpeg", "jpg", "png", "gif", "webp", "bmp", "heic", "heif", "svg")

actual suspend fun localImages(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String> {
    val userHomeDir = File(System.getProperty("user.home"))
    val userPicturesDir = File(userHomeDir, "Pictures")
    val photoList = mutableListOf<String>()
    var index = -1
    userPicturesDir.walkTopDown().forEach { file ->
        if (file.isFile) {
            val extension = file.extension
            if (acceptImageExtensions.contains(extension.lowercase(Locale.getDefault()))) {
                index++
                if (index >= startPosition && index < startPosition + pageSize) {
                    photoList.add(file.path)
                }
                if (photoList.size >= pageSize) {
                    return@forEach
                }
            }
        }
    }
    return photoList
}