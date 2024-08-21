package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ByteArrayImageSourceTest {

    @Test
    fun testFromByteArray() {
        val string1 = "1234567890"
        val string2 = "abcdefghij"

        assertEquals(
            expected = ByteArrayImageSource(string1.encodeToByteArray()),
            actual = ImageSource.fromByteArray(string1.encodeToByteArray())
        )

        assertEquals(
            expected = ByteArrayImageSource(string2.encodeToByteArray()),
            actual = ImageSource.fromByteArray(string2.encodeToByteArray())
        )

        assertNotEquals(
            illegal = ByteArrayImageSource(string1.encodeToByteArray()),
            actual = ImageSource.fromByteArray(string2.encodeToByteArray())
        )
    }

    @Test
    fun testKey() {
        val string1 = "1234567890"
        val string2 = "abcdefghij"

        val bytes1 = string1.encodeToByteArray()
        val bytes2 = string2.encodeToByteArray()
        assertEquals(
            expected = bytes1.toString(),
            actual = ByteArrayImageSource(bytes1).key
        )
        assertEquals(
            expected = bytes2.toString(),
            actual = ByteArrayImageSource(bytes2).key
        )
    }

    @Test
    fun testOpenSource() {
        val string1 = "1234567890"
        val string2 = "abcdefghij"

        assertEquals(
            expected = string1,
            actual = ByteArrayImageSource(string1.encodeToByteArray()).openSource().buffer().use {
                it.readByteArray().decodeToString()
            }
        )

        assertEquals(
            expected = string2,
            actual = ByteArrayImageSource(string2.encodeToByteArray()).openSource().buffer().use {
                it.readByteArray().decodeToString()
            }
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val string1 = "1234567890"
        val string2 = "abcdefghij"

        val source1 = ByteArrayImageSource(string1.encodeToByteArray())
        val source12 = ByteArrayImageSource(string1.encodeToByteArray())
        val source2 = ByteArrayImageSource(string2.encodeToByteArray())
        val source22 = ByteArrayImageSource(string2.encodeToByteArray())

        assertEquals(expected = source1, actual = source1)
        assertEquals(expected = source1, actual = source12)
        assertEquals(expected = source2, actual = source22)
        assertNotEquals(illegal = source1, actual = null as Any?)
        assertNotEquals(illegal = source1, actual = Any())
        assertNotEquals(illegal = source1, actual = source2)
        assertNotEquals(illegal = source12, actual = source22)

        assertNotEquals(illegal = source1.hashCode(), actual = source12.hashCode())
        assertNotEquals(illegal = source2.hashCode(), actual = source22.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source2.hashCode())
        assertNotEquals(illegal = source12.hashCode(), actual = source22.hashCode())
    }

    @Test
    fun testToString() {
        val string1 = "1234567890"
        val string2 = "abcdefghij"

        val bytes1 = string1.encodeToByteArray()
        val bytes2 = string2.encodeToByteArray()
        assertEquals(
            expected = "ByteArrayImageSource('$bytes1')",
            actual = ByteArrayImageSource(bytes1).toString()
        )
        assertEquals(
            expected = "ByteArrayImageSource('$bytes2')",
            actual = ByteArrayImageSource(bytes2).toString()
        )
    }
}