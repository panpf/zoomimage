package com.githb.panpf.zoomimage.images

import com.github.panpf.sketch.util.Size

open class ImageFile(
    val uri: String,
    val name: String,
    val size: Size,
    val exifOrientation: Int = ExifOrientation.UNDEFINED,
) {
    fun copy(
        uri: String = this.uri,
        name: String = this.name,
        size: Size = this.size,
        exifOrientation: Int = this.exifOrientation,
    ): ImageFile = ImageFile(uri, name, size, exifOrientation)

    override fun toString(): String {
        return "ImageFile(uri='$uri', name='$name', size=$size, exifOrientation=$exifOrientation)"
    }
}

class ResourceImageFile(
    val resourceName: String,
    name: String,
    size: Size,
    exifOrientation: Int = ExifOrientation.UNDEFINED
) : ImageFile(
    uri = resourceNameToUri(resourceName),
    name = name,
    size = size,
    exifOrientation = exifOrientation,
) {

    override fun toString(): String {
        return "ResourceImageFile(resourceName='$resourceName', uri='$uri', name='$name', size=$size, exifOrientation=$exifOrientation)"
    }
}

expect fun resourceNameToUri(name: String): String