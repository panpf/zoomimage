package com.github.panpf.zoomimage.sample.ui.gallery

import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.ImageFile
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.decode.internal.readImageInfo
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.zoomimage.sample.ComposeResourceImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

actual fun builtinImages(): List<ImageFile> {
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
    )
}

actual suspend fun readPhotosFromPhotoAlbum(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String> = emptyList()   // TODO Read ios photo album

actual suspend fun readImageInfoOrNull(
    context: PlatformContext,
    sketch: Sketch,
    uri: String,
): ImageInfo? = withContext(Dispatchers.IO) {
    runCatching {
        val fetcher = sketch.components.newFetcherOrThrow(ImageRequest(context, uri))
        val dataSource = fetcher.fetch().getOrThrow().dataSource
        dataSource.readImageInfo()
    }.apply {
        if (isFailure) {
            exceptionOrNull()?.printStackTrace()
        }
    }.getOrNull()
}