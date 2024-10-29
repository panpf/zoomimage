package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.buffer
import okio.use
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.use


fun ImageSource.decodeImageInfo(): ImageInfo {
    val bytes = openSource().buffer().use { it.readByteArray() }
    val image = Image.makeFromEncoded(bytes)
    val encodedImageFormat = Codec.makeFromData(Data.makeFromBytes(bytes)).use {
        it.encodedImageFormat
    }
    val mimeType = "image/${encodedImageFormat.name.lowercase()}"
    return ImageInfo(
        width = image.width,
        height = image.height,
        mimeType = mimeType
    )
}