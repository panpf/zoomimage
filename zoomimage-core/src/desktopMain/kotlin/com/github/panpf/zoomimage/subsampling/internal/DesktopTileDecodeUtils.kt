package com.github.panpf.zoomimage.subsampling.internal

import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream


@WorkerThread
internal fun ImageSource.readImageInfo(): Result<ImageInfo> {
    val inputStream: InputStream = openInputStream()
        .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
    var imageStream: ImageInputStream? = null
    var reader: ImageReader? = null
    try {
        imageStream = ImageIO.createImageInputStream(inputStream)
        reader = ImageIO.getImageReaders(imageStream).next().apply {
            setInput(imageStream, true, true)
        }
        val width = reader.getWidth(0)
        val height = reader.getHeight(0)
        val mimeType = "image/${reader.formatName.lowercase()}"
        return Result.success(ImageInfo(width = width, height = height, mimeType = mimeType))
    } catch (e: Throwable) {
        return Result.failure(e)
    } finally {
        reader?.dispose()
        imageStream?.close()
        inputStream.close()
    }
}