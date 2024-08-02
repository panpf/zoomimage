package com.githb.panpf.zoomimage.images

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ContentImages private constructor(val context: Context) {

    companion object {

        suspend fun create(context: Context): ContentImages {
            saveToExternalFilesDir(context)
            return ContentImages(context)
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

    private val path = "content://${context.packageName}.fileprovider/asset_images/"

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

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic
    )
}