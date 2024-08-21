package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromByteArray
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class ImageSourceTest {

    @Test
    fun testWrapperFactoryConstructor() {
        val imageSource1 = ImageSource.fromByteArray("1234567890".encodeToByteArray())
        val imageSource2 = ImageSource.fromByteArray("abcdefghij".encodeToByteArray())

        val factory1 = ImageSource.WrapperFactory(imageSource1)
        val factory2 = ImageSource.WrapperFactory(imageSource2)

        assertSame(expected = imageSource1, actual = factory1.imageSource)
        assertSame(expected = imageSource1.key, actual = factory1.key)
        assertSame(expected = imageSource2, actual = factory2.imageSource)
        assertSame(expected = imageSource2.key, actual = factory2.key)
    }

    @Test
    fun testWrapperFactoryCrate() = runTest {
        val imageSource1 = ImageSource.fromByteArray("1234567890".encodeToByteArray())
        val imageSource2 = ImageSource.fromByteArray("abcdefghij".encodeToByteArray())

        val factory1 = ImageSource.WrapperFactory(imageSource1)
        val factory2 = ImageSource.WrapperFactory(imageSource2)

        assertSame(expected = imageSource1, actual = factory1.create())
        assertSame(expected = imageSource2, actual = factory2.create())
    }

    @Test
    fun testWrapperFactoryEqualsAndHashCode() {
        val imageSource1 = ImageSource.fromByteArray("1234567890".encodeToByteArray())
        val imageSource2 = ImageSource.fromByteArray("abcdefghij".encodeToByteArray())
        val imageSource12 = ImageSource.fromByteArray("1234567890".encodeToByteArray())
        val imageSource22 = ImageSource.fromByteArray("abcdefghij".encodeToByteArray())

        val factory1 = ImageSource.WrapperFactory(imageSource1)
        val factory2 = ImageSource.WrapperFactory(imageSource2)
        val factory12 = ImageSource.WrapperFactory(imageSource12)
        val factory22 = ImageSource.WrapperFactory(imageSource22)

        assertEquals(expected = factory1, actual = factory1)
        assertEquals(expected = factory1, actual = factory12)
        assertEquals(expected = factory2, actual = factory22)
        assertNotEquals(illegal = factory1, actual = null as Any?)
        assertNotEquals(illegal = factory1, actual = Any())
        assertNotEquals(illegal = factory1, actual = factory2)
        assertNotEquals(illegal = factory12, actual = factory22)

        assertNotEquals(illegal = factory1.hashCode(), actual = factory12.hashCode())
        assertNotEquals(illegal = factory2.hashCode(), actual = factory22.hashCode())
        assertNotEquals(illegal = factory1.hashCode(), actual = factory2.hashCode())
        assertNotEquals(illegal = factory12.hashCode(), actual = factory22.hashCode())
    }

    @Test
    fun testWrapperFactoryToString() {
        val imageSource1 = ImageSource.fromByteArray("1234567890".encodeToByteArray())
        val imageSource2 = ImageSource.fromByteArray("abcdefghij".encodeToByteArray())

        val factory1 = ImageSource.WrapperFactory(imageSource1)
        val factory2 = ImageSource.WrapperFactory(imageSource2)

        assertEquals(
            expected = "WrapperFactory($imageSource1)",
            actual = factory1.toString()
        )
        assertEquals(
            expected = "WrapperFactory($imageSource2)",
            actual = factory2.toString()
        )
    }

    @Test
    fun testToFactory() {
        val imageSource = ImageSource.fromByteArray("1234567890".encodeToByteArray())
        val factory = imageSource.toFactory()
        assertSame(
            expected = imageSource,
            actual = (factory as ImageSource.WrapperFactory).imageSource
        )
        assertSame(expected = imageSource.key, actual = factory.key)
    }
}