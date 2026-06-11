package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat

object HttpImageFiles {
    val hugeLongComic: ImageFile = ComposeResImageFiles.hugeLongQmsht.toHttpImageFile()
}

class HttpImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED
) : ImageFile {

    override val uri: String = "http://img.panpengfei.com/$name"

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    override fun toString(): String =
        "HttpImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toHttpImageFile(): HttpImageFile = HttpImageFile(
    name = this.name,
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)