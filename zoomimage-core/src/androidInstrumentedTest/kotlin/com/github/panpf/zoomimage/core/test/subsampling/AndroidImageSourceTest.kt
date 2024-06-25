package com.github.panpf.zoomimage.core.test.subsampling

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class AndroidImageSourceTest {

    @Test
    fun testAssetImageSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        AssetImageSource(context, "sample_cat.jpg").apply {
            Assert.assertEquals("asset://sample_cat.jpg", key)
            Assert.assertEquals("AssetImageSource('sample_cat.jpg')", toString())
            openSource().getOrThrow().close()
        }

        AssetImageSource(context, "sample_dog.jpg").apply {
            Assert.assertEquals("asset://sample_dog.jpg", key)
            Assert.assertEquals("AssetImageSource('sample_dog.jpg')", toString())
            openSource().getOrThrow().close()
        }
    }

    @Test
    fun testResourceImageSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        val drawableId = com.github.panpf.zoomimage.images.R.raw.sample_anim
        ResourceImageSource(context, drawableId).apply {
            Assert.assertEquals("android.resources://resource?resId=$drawableId", key)
            Assert.assertEquals("ResourceImageSource($drawableId)", toString())
            openSource().getOrThrow().close()
        }
    }

    @Test
    fun testContentImageSource() {
        val context = InstrumentationRegistry.getInstrumentation().context

        val uri =
            Uri.parse("content://com.github.panpf.zoomimage.core.test.fileprovider/sample_anim.gif")
        ContentImageSource(context, uri).apply {
            Assert.assertEquals(uri.toString(), key)
            Assert.assertEquals("ContentImageSource('$uri')", toString())
        }
    }
}