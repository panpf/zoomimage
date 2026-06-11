package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat

object AssetImageFiles {
    val cat: AssetImageFile = ComposeResImageFiles.cat.toAssetImageFile()
    val dog: AssetImageFile = ComposeResImageFiles.dog.toAssetImageFile()
    val hugeChina: AssetImageFile = ComposeResImageFiles.hugeChina.toAssetImageFile()
    val hugeChinaThumbnail: AssetImageFile =
        ComposeResImageFiles.hugeChinaThumbnail.toAssetImageFile()
    val hugeLongComic: AssetImageFile = ComposeResImageFiles.hugeLongComic.toAssetImageFile()
    val hugeLongComicThumbnail: AssetImageFile =
        ComposeResImageFiles.hugeLongComicThumbnail.toAssetImageFile()
    val hugeLongQmsht: AssetImageFile = ComposeResImageFiles.hugeLongQmsht.toAssetImageFile()
    val hugeLongQmshtThumbnail: AssetImageFile =
        ComposeResImageFiles.hugeLongQmshtThumbnail.toAssetImageFile()
    val longEnd: AssetImageFile = ComposeResImageFiles.longEnd.toAssetImageFile()
    val longWhale: AssetImageFile = ComposeResImageFiles.longWhale.toAssetImageFile()

    val values: Array<AssetImageFile> = arrayOf(
        cat,
        dog,
        longEnd,
        longWhale,
        hugeChina,
        hugeLongComic,
        hugeLongQmsht,
    )
}

class AssetImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    override val uri = "file:///android_asset/$name"

    override fun toString(): String =
        "AssetImageFile(name='$name', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toAssetImageFile(): AssetImageFile = AssetImageFile(
    name = this.name,
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)