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
        val name = AssetImageFiles.cat.name
        assertEquals(
            expected = AssetImageSource(context, name),
            actual = ImageSource.fromAsset(context, name)
        )
    }

    @Test
    fun testKey() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val name = AssetImageFiles.cat.name
        assertEquals(
            expected = "file:///android_asset/$name",
            actual = AssetImageSource(context, name).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        AssetImageSource(context, AssetImageFiles.cat.name)
            .openSource().buffer()
            .use { it.readByteArray() }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val name = AssetImageFiles.cat.name
        val name2 = "${name}_fake"

        val source1 = AssetImageSource(context, name)
        val source12 = AssetImageSource(context, name)
        val source2 = AssetImageSource(context, name2)
        val source22 = AssetImageSource(context, name2)

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
        val name = AssetImageFiles.cat.name

        assertEquals(
            expected = "AssetImageSource('$name')",
            actual = AssetImageSource(context, name).toString()
        )
    }
}