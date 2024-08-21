package com.github.panpf.zoomimage.core.android.test.subsampling

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.fromResource
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ResourceImageSourceTest {

    @Test
    fun testFromResource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        assertEquals(
            expected = ResourceImageSource(context, 111),
            actual = ImageSource.fromResource(context, 111)
        )
        assertEquals(
            expected = ResourceImageSource(context, 222),
            actual = ImageSource.fromResource(context.resources, 222)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        assertEquals(
            expected = "android.resources:///111",
            actual = ResourceImageSource(context, 111).key
        )
        assertEquals(
            expected = "android.resources:///222",
            actual = ResourceImageSource(context, 222).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        ResourceImageSource(context, com.github.panpf.zoomimage.images.R.raw.huge_card).openSource()
            .buffer().use {
                it.readByteArray()
            }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        val source1 = ResourceImageSource(context, 111)
        val source12 = ResourceImageSource(context, 111)
        val source2 = ResourceImageSource(context, 222)
        val source22 = ResourceImageSource(context, 222)

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

        assertEquals(
            expected = "ResourceImageSource(111)",
            actual = ResourceImageSource(context, 111).toString()
        )
        assertEquals(
            expected = "ResourceImageSource(222)",
            actual = ResourceImageSource(context, 222).toString()
        )
    }
}