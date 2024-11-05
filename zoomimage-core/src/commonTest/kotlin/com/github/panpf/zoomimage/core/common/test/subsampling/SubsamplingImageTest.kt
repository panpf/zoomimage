package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.toFactory
import com.github.panpf.zoomimage.test.TestImageSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SubsamplingImageTest {

    @Test
    fun testConstructor() {
        SubsamplingImage(TestImageSource())

        SubsamplingImage(TestImageSource().toFactory())
    }

    @Test
    fun testKey() {
        assertEquals(
            expected = "TestImageSource&imageInfo=null",
            actual = SubsamplingImage(TestImageSource()).key
        )

        assertEquals(
            expected = "TestImageSource&imageInfo=ImageInfo(size=101x202, mimeType='image/jpeg')",
            actual = SubsamplingImage(TestImageSource(), ImageInfo(101, 202, "image/jpeg")).key
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val imageSource1 = TestImageSource()
        val imageSource2 = TestImageSource()
        val element1 = SubsamplingImage(imageSource1)
        val element11 = SubsamplingImage(imageSource1)
        val element2 = SubsamplingImage(imageSource2)
        val element3 = SubsamplingImage(imageSource2, ImageInfo(101, 202, "image/jpeg"))

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = element3)
        assertNotEquals(illegal = element2, actual = element3)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element3.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element3.hashCode())
    }

    @Test
    fun testToString() {
        val imageSource = ImageSource.WrapperFactory(TestImageSource())
        val imageInfo = ImageInfo(101, 202, "image/jpeg")
        assertEquals(
            expected = "SubsamplingImage(imageSource=$imageSource, imageInfo=$imageInfo)",
            actual = SubsamplingImage(imageSource, imageInfo).toString()
        )
    }
}