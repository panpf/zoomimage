package com.github.panpf.zoomimage.core.android.test.subsampling

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.images.AssetImageFiles
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
        val fileName = AssetImageFiles.cat.fileName
        assertEquals(
            expected = AssetImageSource(context, fileName),
            actual = ImageSource.fromAsset(context, fileName)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val fileName = AssetImageFiles.cat.fileName
        assertEquals(
            expected = "file:///android_asset/$fileName",
            actual = AssetImageSource(context, fileName).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        AssetImageSource(context, AssetImageFiles.cat.fileName)
            .openSource().buffer()
            .use { it.readByteArray() }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val fileName = AssetImageFiles.cat.fileName
        val fileName2 = "${fileName}_fake"

        val source1 = AssetImageSource(context, fileName)
        val source12 = AssetImageSource(context, fileName)
        val source2 = AssetImageSource(context, fileName2)
        val source22 = AssetImageSource(context, fileName2)

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
        val fileName = AssetImageFiles.cat.fileName

        assertEquals(
            expected = "AssetImageSource('$fileName')",
            actual = AssetImageSource(context, fileName).toString()
        )
    }
}