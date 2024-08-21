package com.github.panpf.zoomimage.core.android.test.subsampling

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ContentImages
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromContent
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ContentImageSourceTest {

    @Test
    fun testFromContent() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val contentImages = ContentImages.create(context)
        val uri1 = contentImages.cat.uri.toUri()
        val uri2 = contentImages.dog.uri.toUri()

        assertEquals(
            expected = ContentImageSource(context, uri1),
            actual = ImageSource.fromContent(context, uri1)
        )

        assertEquals(
            expected = ContentImageSource(context, uri2),
            actual = ImageSource.fromContent(context, uri2)
        )

        assertNotEquals(
            illegal = ContentImageSource(context, uri1),
            actual = ImageSource.fromContent(context, uri2)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val contentImages = ContentImages.create(context)
        val uri1 = contentImages.cat.uri.toUri()
        val uri2 = contentImages.dog.uri.toUri()

        assertEquals(
            expected = uri1.toString(),
            actual = ContentImageSource(context, uri1).key
        )
        assertEquals(
            expected = uri2.toString(),
            actual = ContentImageSource(context, uri2).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val contentImages = ContentImages.create(context)
        val uri1 = contentImages.cat.uri.toUri()
        val uri2 = contentImages.dog.uri.toUri()

        ContentImageSource(context, uri1).openSource().buffer().use {
            it.readByteArray()
        }

        ContentImageSource(context, uri2).openSource().buffer().use {
            it.readByteArray().decodeToString()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val contentImages = ContentImages.create(context)
        val uri1 = contentImages.cat.uri.toUri()
        val uri2 = contentImages.dog.uri.toUri()

        val source1 = ContentImageSource(context, uri1)
        val source12 = ContentImageSource(context, uri1)
        val source2 = ContentImageSource(context, uri2)
        val source22 = ContentImageSource(context, uri2)

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
        val contentImages = ContentImages.create(context)
        val uri1 = contentImages.cat.uri.toUri()
        val uri2 = contentImages.dog.uri.toUri()

        assertEquals(
            expected = "ContentImageSource('$uri1')",
            actual = ContentImageSource(context, uri1).toString()
        )
        assertEquals(
            expected = "ContentImageSource('$uri2')",
            actual = ContentImageSource(context, uri2).toString()
        )
    }
}