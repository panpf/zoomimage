package com.github.panpf.zoomimage.core.android.test.subsampling

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.images.ContentImageFiles
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
        val uri1 = ContentImageFiles.with(context).cat.uri.toUri()
        val uri2 = "${uri1}_1".toUri()

        assertEquals(
            expected = ContentImageSource(context, uri1),
            actual = ImageSource.fromContent(context, uri1)
        )

        assertNotEquals(
            illegal = ContentImageSource(context, uri1),
            actual = ImageSource.fromContent(context, uri2)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val uri1 = ContentImageFiles.with(context).cat.uri.toUri()

        assertEquals(
            expected = uri1.toString(),
            actual = ContentImageSource(context, uri1).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val uri1 = ContentImageFiles.with(context).cat.uri.toUri()
        ContentImageSource(context, uri1).openSource().buffer().use {
            it.readByteArray()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val uri1 = ContentImageFiles.with(context).cat.uri.toUri()
        val uri2 = "${uri1}_1".toUri()

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
        val uri1 = ContentImageFiles.with(context).cat.uri.toUri()

        assertEquals(
            expected = "ContentImageSource('$uri1')",
            actual = ContentImageSource(context, uri1).toString()
        )
    }
}