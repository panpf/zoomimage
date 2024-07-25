package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.SkiaImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.buffer
import okio.use
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.use


fun ImageSource.decodeImageInfo(): ImageInfo {
    val bytes = openSource().buffer().use { it.readByteArray() }
    val skiaImage = SkiaImage.makeFromEncoded(bytes)
    val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
        it.encodedImageFormat
    }
    val mimeType = "image/${encodedImageFormat.name.lowercase()}"
    return ImageInfo(
        width = skiaImage.width,
        height = skiaImage.height,
        mimeType = mimeType
    )
}