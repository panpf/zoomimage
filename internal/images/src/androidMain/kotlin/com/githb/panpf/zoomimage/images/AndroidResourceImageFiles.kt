package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.images.R
import com.github.panpf.zoomimage.util.IntSizeCompat

object AndroidResourceImageFiles {
    val hugeCard: ImageFile = AndroidResourceImageFile(
        resId = R.raw.huge_card,
        name = "huge_card.jpg",
        size = IntSizeCompat(7557, 5669)
    )
}

class AndroidResourceImageFile(
    val resId: Int,
    override val name: String,
    override val size: IntSizeCompat,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val uri = "android.resource:///${resId}"

    override fun toString(): String =
        "AndroidResourceImageFile(resId='$resId', name='$name', size=$size, exifOrientation=$exifOrientation)"
}