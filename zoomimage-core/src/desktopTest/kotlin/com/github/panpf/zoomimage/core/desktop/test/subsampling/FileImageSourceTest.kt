package com.github.panpf.zoomimage.core.desktop.test.subsampling

import com.github.panpf.zoomimage.images.DesktopLocalImages
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromFile
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FileImageSourceTest {

    @Test
    fun testFromFile() = runTest {
        val path1 = DesktopLocalImages.with().cat.uri.replace("file://", "")
        val path2 = "${path1}_1"

        assertEquals(
            expected = FileImageSource(path1.toPath()),
            actual = ImageSource.fromFile(path1.toPath())
        )

        assertNotEquals(
            illegal = FileImageSource(path1.toPath()),
            actual = ImageSource.fromFile(path2)
        )
    }

    @Test
    fun testKey() = runTest {
        val path1 = DesktopLocalImages.with().cat.uri.replace("file://", "")

        assertEquals(
            expected = "file://$path1",
            actual = FileImageSource(path1.toPath()).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val path1 = DesktopLocalImages.with().cat.uri.replace("file://", "")
        FileImageSource(path1.toPath()).openSource().buffer().use {
            it.readByteArray()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val path1 = DesktopLocalImages.with().cat.uri.replace("file://", "")
        val path2 = "${path1}_1"

        val source1 = FileImageSource(path1.toPath())
        val source12 = FileImageSource(path1.toPath())
        val source2 = FileImageSource(path2.toPath())
        val source22 = FileImageSource(path2.toPath())

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
        val path1 = DesktopLocalImages.with().cat.uri.replace("file://", "")

        assertEquals(
            expected = "FileImageSource('$path1')",
            actual = FileImageSource(path1.toPath()).toString()
        )
    }
}