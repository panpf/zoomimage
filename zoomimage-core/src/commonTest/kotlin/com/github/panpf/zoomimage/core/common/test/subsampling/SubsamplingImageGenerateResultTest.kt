package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.TestImageSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SubsamplingImageGenerateResultTest {

    @Test
    fun testSuccessConstructor() {
        SubsamplingImageGenerateResult.Success(SubsamplingImage(TestImageSource()))
    }

    @Test
    fun testSuccessEqualsAndHashCode() {
        val imageSource1 = TestImageSource()
        val imageSource2 = TestImageSource()
        val element1 = SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource1))
        val element11 = SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource1))
        val element2 = SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource2))
        val element3 = SubsamplingImageGenerateResult.Success(
            SubsamplingImage(
                imageSource2,
                ImageInfo(101, 202, "image/jpeg")
            )
        )

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
    fun testSuccessToString() {
        val imageSource = ImageSource.WrapperFactory(TestImageSource())
        val imageInfo = ImageInfo(101, 202, "image/jpeg")
        assertEquals(
            expected = "Success(subsamplingImage=SubsamplingImage(imageSource=$imageSource, imageInfo=$imageInfo))",
            actual = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource,
                    imageInfo
                )
            ).toString()
        )
    }

    @Test
    fun testErrorConstructor() {
        SubsamplingImageGenerateResult.Error("not support")
    }

    @Test
    fun testErrorEqualsAndHashCode() {
        val element1 = SubsamplingImageGenerateResult.Error("not support")
        val element11 = SubsamplingImageGenerateResult.Error("not support")
        val element2 = SubsamplingImageGenerateResult.Error("not support2")

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
    }

    @Test
    fun testErrorToString() {
        assertEquals(
            expected = "Error(message=not support)",
            actual = SubsamplingImageGenerateResult.Error("not support").toString()
        )
    }
}