package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat

object HttpImageFiles {
    val hugeLongComic: ImageFile = HttpImageFile(
        uri = "http://img.panpengfei.com/sample_long_comic.jpg",
        name = "sample_long_comic.jpg",
        size = IntSizeCompat(7557, 5669)
    )
}

class HttpImageFile(
    override val uri: String,
    override val name: String,
    override val size: IntSizeCompat,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED
) : ImageFile {

    override fun toString(): String =
        "HttpImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}