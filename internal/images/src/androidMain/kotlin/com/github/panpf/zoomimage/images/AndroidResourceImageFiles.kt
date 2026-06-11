package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat

object AndroidResourceImageFiles {
    val hugeCard: ImageFile = ComposeResImageFiles.hugeCard
        .toAndroidResourceImageFile(resId = R.raw.huge_card)
}

class AndroidResourceImageFile(
    val resId: Int,
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    override val uri = "android.resource:///${resId}"

    override fun toString(): String =
        "AndroidResourceImageFile(resId='$resId', name='$name', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toAndroidResourceImageFile(resId: Int): AndroidResourceImageFile =
    AndroidResourceImageFile(
        resId = resId,
        name = this.name,
        size = this.size,
        length = this.length,
        mimeType = this.mimeType,
        animated = this.animated,
        exifOrientation = this.exifOrientation
    )