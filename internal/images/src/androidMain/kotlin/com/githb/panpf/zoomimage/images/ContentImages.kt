package com.githb.panpf.zoomimage.images

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ContentImages private constructor() {

    companion object {

        private var instance: ContentImages? = null

        suspend fun with(context: Context): ContentImages {
            saveToExternalFilesDir(context)
            return instance ?: synchronized(this) {
                instance ?: ContentImages().also { instance = it }
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
        "content://com.github.panpf.zoomimage.sample.fileprovider/asset_images/"

    val cat = ResourceImages.cat.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val dog = ResourceImages.dog.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val anim = ResourceImages.anim.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longEnd = ResourceImages.longEnd.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val longWhale = ResourceImages.longWhale.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeChina = ResourceImages.hugeChina.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeCard = ResourceImages.hugeCard.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongQmsht =
        ResourceImages.hugeLongQmsht.let { it.copy(uri = it.uri.replace("asset://", path)) }
    val hugeLongComic =
        ResourceImages.hugeLongComic.let { it.copy(uri = it.uri.replace("asset://", path)) }

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