package com.github.panpf.zoomimage.images

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat

object HttpImageFiles {
    val cat: ImageFile = ComposeResImageFiles.cat.toHttpImageFile()
    val dog: ImageFile = ComposeResImageFiles.dog.toHttpImageFile()
    val giraffe: ImageFile = ComposeResImageFiles.giraffe.toHttpImageFile()
    val horse: ImageFile = ComposeResImageFiles.horse.toHttpImageFile()
    val anim: ImageFile = ComposeResImageFiles.anim.toHttpImageFile()
    val longEnd: ImageFile = ComposeResImageFiles.longEnd.toHttpImageFile()
    val longWhale: ImageFile = ComposeResImageFiles.longWhale.toHttpImageFile()
    val hugeCard: ImageFile = ComposeResImageFiles.hugeCard.toHttpImageFile()
    val hugeCardThumbnail: ImageFile = ComposeResImageFiles.hugeCardThumbnail.toHttpImageFile()
    val hugeChina: ImageFile = ComposeResImageFiles.hugeChina.toHttpImageFile()
    val hugeChinaThumbnail: ImageFile = ComposeResImageFiles.hugeChinaThumbnail.toHttpImageFile()
    val hugeLongComic: ImageFile = ComposeResImageFiles.hugeLongComic.toHttpImageFile()
    val hugeLongComicThumbnail: ImageFile =
        ComposeResImageFiles.hugeLongComicThumbnail.toHttpImageFile()
    val hugeLongQmsht: ImageFile = ComposeResImageFiles.hugeLongQmsht.toHttpImageFile()
    val hugeLongQmshtThumbnail: ImageFile =
        ComposeResImageFiles.hugeLongQmshtThumbnail.toHttpImageFile()
    val exifFlipHorizontal: ImageFile = ComposeResImageFiles.exifFlipHorizontal.toHttpImageFile()
    val exifFlipVertical: ImageFile = ComposeResImageFiles.exifFlipVertical.toHttpImageFile()
    val exifNormal: ImageFile = ComposeResImageFiles.exifNormal.toHttpImageFile()
    val exifRotate90: ImageFile = ComposeResImageFiles.exifRotate90.toHttpImageFile()
    val exifRotate180: ImageFile = ComposeResImageFiles.exifRotate180.toHttpImageFile()
    val exifRotate270: ImageFile = ComposeResImageFiles.exifRotate270.toHttpImageFile()
    val exifTranspose: ImageFile = ComposeResImageFiles.exifTranspose.toHttpImageFile()
    val exifTransverse: ImageFile = ComposeResImageFiles.exifTransverse.toHttpImageFile()
    val woodpile: ImageFile = ComposeResImageFiles.woodpile.toHttpImageFile()
}

class HttpImageFile(
    override val name: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED
) : ImageFile {

    override val uri: String = "https://panpf.github.io/zoomimage/app/files/$name"

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