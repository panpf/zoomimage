package com.github.panpf.zoomimage.images

import android.content.Context
import android.net.Uri
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.jetbrains.compose.resources.InternalResourceApi

object AssetImageFiles {
    val cat = ComposeResImageFiles.cat.toAssetImageFile()
    val dog = ComposeResImageFiles.dog.toAssetImageFile()
    val giraffe = ComposeResImageFiles.giraffe.toAssetImageFile()
    val horse = ComposeResImageFiles.horse.toAssetImageFile()
    val anim = ComposeResImageFiles.anim.toAssetImageFile()
    val longEnd = ComposeResImageFiles.longEnd.toAssetImageFile()
    val longWhale = ComposeResImageFiles.longWhale.toAssetImageFile()
    val hugeCard = ComposeResImageFiles.hugeCard.toAssetImageFile()
    val hugeCardThumbnail = ComposeResImageFiles.hugeCardThumbnail.toAssetImageFile()
    val hugeChina = ComposeResImageFiles.hugeChina.toAssetImageFile()
    val hugeChinaThumbnail = ComposeResImageFiles.hugeChinaThumbnail.toAssetImageFile()
    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toAssetImageFile()
    val hugeLongComicThumbnail = ComposeResImageFiles.hugeLongComicThumbnail.toAssetImageFile()
    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toAssetImageFile()
    val hugeLongQmshtThumbnail = ComposeResImageFiles.hugeLongQmshtThumbnail.toAssetImageFile()
    val exifFlipHorizontal = ComposeResImageFiles.exifFlipHorizontal.toAssetImageFile()
    val exifFlipVertical = ComposeResImageFiles.exifFlipVertical.toAssetImageFile()
    val exifNormal = ComposeResImageFiles.exifNormal.toAssetImageFile()
    val exifRotate90 = ComposeResImageFiles.exifRotate90.toAssetImageFile()
    val exifRotate180 = ComposeResImageFiles.exifRotate180.toAssetImageFile()
    val exifRotate270 = ComposeResImageFiles.exifRotate270.toAssetImageFile()
    val exifTranspose = ComposeResImageFiles.exifTranspose.toAssetImageFile()
    val exifTransverse = ComposeResImageFiles.exifTransverse.toAssetImageFile()
    val woodpile = ComposeResImageFiles.woodpile.toAssetImageFile()
}

class AssetImageFile(
    override val name: String,
    override val uri: String,
    override val size: IntSizeCompat,
    override val length: Long,
    override val mimeType: String,
    override val animated: Boolean = false,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override val imageInfo: ImageInfo = ImageInfo(size = size, mimeType = mimeType)

    val fileName = Uri.parse(uri).pathSegments.drop(1).joinToString("/")

    val sketch3Uri: String = "asset://$fileName"

    @OptIn(InternalResourceApi::class)
    fun toImageSource(context: Context): ImageSource {
        return AssetImageSource(context, fileName)
    }

    override fun toString(): String =
        "AssetImageFile(name='$name', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toAssetImageFile(): AssetImageFile = AssetImageFile(
    name = this.name,
    uri = this.uri.replace("/compose_resource/", "/android_asset/"),
    size = this.size,
    length = this.length,
    mimeType = this.mimeType,
    animated = this.animated,
    exifOrientation = this.exifOrientation
)