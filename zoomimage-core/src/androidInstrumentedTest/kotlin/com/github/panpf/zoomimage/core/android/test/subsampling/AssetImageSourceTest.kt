package com.github.panpf.zoomimage.core.android.test.subsampling

import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AssetImageSourceTest {

    @Test
    fun testFromAsset() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val resourceName1 = resourceImages.cat.resourceName
        val resourceName2 = resourceImages.dog.resourceName

        assertEquals(
            expected = AssetImageSource(context, resourceName1),
            actual = ImageSource.fromAsset(context, resourceName1)
        )

        assertEquals(
            expected = AssetImageSource(context, resourceName2),
            actual = ImageSource.fromAsset(context, resourceName2)
        )

        assertNotEquals(
            illegal = AssetImageSource(context, resourceName1),
            actual = ImageSource.fromAsset(context, resourceName2)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val resourceName1 = resourceImages.cat.resourceName
        val resourceName2 = resourceImages.dog.resourceName

        assertEquals(
            expected = "file:///android_asset/$resourceName1",
            actual = AssetImageSource(context, resourceName1).key
        )
        assertEquals(
            expected = "file:///android_asset/$resourceName2",
            actual = AssetImageSource(context, resourceName2).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val resourceName1 = resourceImages.cat.resourceName
        val resourceName2 = resourceImages.dog.resourceName

        AssetImageSource(context, resourceName1).openSource().buffer().use {
            it.readByteArray()
        }

        AssetImageSource(context, resourceName2).openSource().buffer().use {
            it.readByteArray().decodeToString()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val resourceImages = ResourceImages
        val resourceName1 = resourceImages.cat.resourceName
        val resourceName2 = resourceImages.dog.resourceName

        val source1 = AssetImageSource(context, resourceName1)
        val source12 = AssetImageSource(context, resourceName1)
        val source2 = AssetImageSource(context, resourceName2)
        val source22 = AssetImageSource(context, resourceName2)

        assertEquals(expected = source1, actual = source1)
        assertEquals(expected = source1, actual = source12)
        assertEquals(expected = source2, actual = source22)
        assertNotEquals(illegal = source1, actual = null as Any?)
        assertNotEquals(illegal = source1, actual = Any())
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
        val resourceName1 = resourceImages.cat.resourceName
        val resourceName2 = resourceImages.dog.resourceName

        assertEquals(
            expected = "AssetImageSource('$resourceName1')",
            actual = AssetImageSource(context, resourceName1).toString()
        )
        assertEquals(
            expected = "AssetImageSource('$resourceName2')",
            actual = AssetImageSource(context, resourceName2).toString()
        )
    }
}