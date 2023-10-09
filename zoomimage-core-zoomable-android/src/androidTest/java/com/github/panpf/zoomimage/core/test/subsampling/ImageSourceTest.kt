package com.github.panpf.zoomimage.core.test.subsampling

import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import org.junit.Assert
import org.junit.Test
import java.io.File

class ImageSourceTest {

    @Test
    fun testKey() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromResource(context, android.R.drawable.bottom_bar).apply {
            Assert.assertEquals("android.resources://resource?resId=$resId", key)
        }

        ImageSource.fromAsset(context, "sample_dog.jpg").apply {
            Assert.assertEquals("asset://$assetFileName", key)
        }

        val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
        ImageSource.fromFile(File(assetsDir, "sample_cat.jpg")).apply {
            Assert.assertEquals(file.path, key)
        }

        ImageSource.fromContent(
            context,
            Uri.parse("content://com.github.panpf.zoomimage.core.test.fileprovider/asset_images/sample_cat.jpg")
        ).apply {
            Assert.assertEquals(uri.toString(), key)
        }
    }

    @Test
    fun testString() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromResource(context, android.R.drawable.bottom_bar).apply {
            Assert.assertEquals("ResourceImageSource($resId)", toString())
        }

        ImageSource.fromAsset(context, "sample_dog.jpg").apply {
            Assert.assertEquals("AssetImageSource('$assetFileName')", toString())
        }

        val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
        ImageSource.fromFile(File(assetsDir, "sample_cat.jpg")).apply {
            Assert.assertEquals("FileImageSource('$file')", toString())
        }

        ImageSource.fromContent(
            context,
            Uri.parse("content://com.github.panpf.zoomimage.core.test.fileprovider/asset_images/sample_cat.jpg")
        ).apply {
            Assert.assertEquals("ContentImageSource('$uri')", toString())
        }
    }

    @Test
    fun testOpenInputStream() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromResource(context, android.R.drawable.bottom_bar).apply {
            Assert.assertNotNull(readImageInfo(false).getOrNull())
        }

        ImageSource.fromAsset(context, "sample_dog.jpg").apply {
            Assert.assertNotNull(readImageInfo(false).getOrNull())
        }

        exportAssetImages(context)
        val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
        ImageSource.fromFile(File(assetsDir, "sample_cat.jpg")).apply {
            Assert.assertNotNull(readImageInfo(false).getOrNull())
        }

        ImageSource.fromContent(
            context,
            Uri.parse("content://com.github.panpf.zoomimage.core.test.fileprovider/asset_images/sample_cat.jpg")
        ).apply {
            Assert.assertNotNull(readImageInfo(false).getOrNull())
        }
    }

    private fun exportAssetImages(context: Context) {
        val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }
        listOf("sample_cat.jpg", "sample_dog.jpg").forEach {
            val file = File(assetsDir, it)
            if (!file.exists()) {
                context.assets.open(it).use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}