package com.github.panpf.zoomimage.core.android.test.subsampling

import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AssetImageSourceTest {

    @Test
    fun testFromAsset() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val uri1 = resourceImages.cat.uri
        val uri2 = resourceImages.dog.uri

        assertEquals(
            expected = AssetImageSource(context, uri1),
            actual = ImageSource.fromAsset(context, uri1)
        )

        assertEquals(
            expected = AssetImageSource(context, uri2),
            actual = ImageSource.fromAsset(context, uri2)
        )

        assertNotEquals(
            illegal = AssetImageSource(context, uri1),
            actual = ImageSource.fromAsset(context, uri2)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val uri1 = resourceImages.cat.resourceName
        val uri2 = resourceImages.dog.resourceName

        assertEquals(
            expected = "asset://$uri1",
            actual = AssetImageSource(context, uri1).key
        )
        assertEquals(
            expected = "asset://$uri2",
            actual = AssetImageSource(context, uri2).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val uri1 = resourceImages.cat.uri
        val uri2 = resourceImages.dog.uri

        AssetImageSource(context, uri1).openSource().buffer().use {
            it.readByteArray()
        }

        AssetImageSource(context, uri2).openSource().buffer().use {
            it.readByteArray().decodeToString()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val uri1 = resourceImages.cat.uri
        val uri2 = resourceImages.dog.uri

        val source1 = AssetImageSource(context, uri1)
        val source12 = AssetImageSource(context, uri1)
        val source2 = AssetImageSource(context, uri2)
        val source22 = AssetImageSource(context, uri2)

        assertEquals(expected = source1, actual = source12)
        assertEquals(expected = source2, actual = source22)
        assertNotEquals(illegal = source1, actual = source2)
        assertNotEquals(illegal = source12, actual = source22)

        assertEquals(expected = source1.hashCode(), actual = source12.hashCode())
        assertEquals(expected = source2.hashCode(), actual = source22.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source2.hashCode())
        assertNotEquals(illegal = source12.hashCode(), actual = source22.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val uri1 = resourceImages.cat.uri
        val uri2 = resourceImages.dog.uri

        assertEquals(
            expected = "AssetImageSource('$uri1')",
            actual = AssetImageSource(context, uri1).toString()
        )
        assertEquals(
            expected = "AssetImageSource('$uri2')",
            actual = AssetImageSource(context, uri2).toString()
        )
    }
}