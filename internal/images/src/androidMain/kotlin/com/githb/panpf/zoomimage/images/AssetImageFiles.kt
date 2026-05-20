package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat

object AssetImageFiles {
    val cat: AssetImageFile = AssetImageFile(
        name = "cat.jpg",
        size = IntSizeCompat(width = 1100, height = 1650)
    )
    val longEnd: AssetImageFile = AssetImageFile(
        name = "long_end.jpg",
        size = IntSizeCompat(width = 2000, height = 618),
        exifOrientation = ExifOrientation.NORMAL
    )
}

class AssetImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val uri = "file:///android_asset/$name"

    override fun toString(): String =
        "AssetImageFile(name='$name', size=$size, exifOrientation=$exifOrientation)"
}