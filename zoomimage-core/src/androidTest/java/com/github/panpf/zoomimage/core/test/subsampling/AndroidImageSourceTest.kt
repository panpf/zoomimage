package com.github.panpf.zoomimage.core.test.subsampling

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import org.junit.Assert
import org.junit.Test

class AndroidImageSourceTest {

    @Test
    fun testAssetImageSource() {
        val context = InstrumentationRegistry.getInstrumentation().context

        AssetImageSource(context, "sample_cat.jpg").apply {
            Assert.assertEquals("asset://sample_cat.jpg", key)
            Assert.assertEquals("AssetImageSource('sample_cat.jpg')", toString())
            openInputStream().getOrThrow().close()
        }

        AssetImageSource(context, "sample_dog.jpg").apply {
            Assert.assertEquals("asset://sample_dog.jpg", key)
            Assert.assertEquals("AssetImageSource('sample_dog.jpg')", toString())
            openInputStream().getOrThrow().close()
        }
    }

    @Test
    fun testResourceImageSource() {
        val context = InstrumentationRegistry.getInstrumentation().context

        val drawableId = com.github.panpf.zoomimage.resources.R.raw.sample_anim
        ResourceImageSource(context, drawableId).apply {
            Assert.assertEquals("android.resources://resource?resId=$drawableId", key)
            Assert.assertEquals("ResourceImageSource($drawableId)", toString())
            openInputStream().getOrThrow().close()
        }
    }

    @Test
    fun testContentImageSource() {
        val context = InstrumentationRegistry.getInstrumentation().context

        val uri = Uri.parse("content://com.github.panpf.zoomimage.core.test.fileprovider/sample_anim.gif")
        ContentImageSource(context, uri).apply {
            Assert.assertEquals(uri.toString(), key)
            Assert.assertEquals("ContentImageSource('$uri')", toString())
        }
    }
}