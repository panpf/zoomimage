package com.githb.panpf.zoomimage.images

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidLocalImages private constructor() {

    companion object {

        private var instance: AndroidLocalImages? = null

        suspend fun with(context: Context): AndroidLocalImages {
            saveToExternalFilesDir(context)
            return instance ?: synchronized(this) {
                instance ?: AndroidLocalImages().also { instance = it }
            }
        }

        suspend fun saveToExternalFilesDir(context: Context) = withContext(Dispatchers.IO) {
            val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            ResourceImages.values.forEach {
                val file = File(assetsDir, it.resourceName)
                if (!file.exists()) {
                    try {
                        context.assets.open(it.resourceName).use { inputStream ->
                            file.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        file.delete()
                    }
                }
            }
        }
    }

    private val path =
        "file://${Environment.getExternalStorageDirectory()}/Android/data/com.github.panpf.zoomimage.sample/files/assets/"

    val cat = ResourceImages.cat.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val dog = ResourceImages.dog.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val anim = ResourceImages.anim.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val longEnd = ResourceImages.longEnd.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val longWhale = ResourceImages.longWhale.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val hugeChina = ResourceImages.hugeChina.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val hugeCard = ResourceImages.hugeCard.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val hugeLongQmsht =
        ResourceImages.hugeLongQmsht.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val hugeLongComic =
        ResourceImages.hugeLongComic.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }

    val exifFlipHorizontal =
        ResourceImages.exifFlipHorizontal.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifFlipVertical =
        ResourceImages.exifFlipVertical.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifNormal =
        ResourceImages.exifNormal.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifRotate90 =
        ResourceImages.exifRotate90.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifRotate180 =
        ResourceImages.exifRotate180.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifRotate270 =
        ResourceImages.exifRotate270.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifTranspose =
        ResourceImages.exifTranspose.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }
    val exifTransverse =
        ResourceImages.exifTransverse.let { it.copy(uri = it.uri.replace("file:///android_asset/", path)) }

    val exifs = arrayOf(
        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic,

        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )
}