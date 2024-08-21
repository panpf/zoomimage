package com.github.panpf.zoomimage.core.ios.test.subsampling

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.KotlinResourceImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class KotlinResourceImageSourceTest {

    @Test
    fun testFromKotlinResource() = runTest {
        val resourceName1 = ResourceImages.cat.resourceName
        val resourceName2 = ResourceImages.dog.resourceName

        assertEquals(
            expected = KotlinResourceImageSource(resourceName1),
            actual = ImageSource.fromKotlinResource(resourceName1)
        )

        assertEquals(
            expected = KotlinResourceImageSource(resourceName2),
            actual = ImageSource.fromKotlinResource(resourceName2)
        )

        assertNotEquals(
            illegal = KotlinResourceImageSource(resourceName1),
            actual = ImageSource.fromKotlinResource(resourceName2)
        )
    }

    @Test
    fun testKey() = runTest {
        val resourceName1 = ResourceImages.cat.resourceName
        val resourceName2 = ResourceImages.dog.resourceName

        assertEquals(
            expected = "file:///kotlin_resource/$resourceName1",
            actual = KotlinResourceImageSource(resourceName1).key
        )
        assertEquals(
            expected = "file:///kotlin_resource/$resourceName2",
            actual = KotlinResourceImageSource(resourceName2).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        // TODO test: Files in kotlin resources cannot be accessed in ios test environment.
        //      There are other places where this problem also occurs, search for it
//        val resourceName1 = ResourceImages.cat.resourceName
//        val resourceName2 = ResourceImages.dog.resourceName
//
//        KotlinResourceImageSource(resourceName1).openSource().buffer().use {
//            it.readByteArray()
//        }
//
//        KotlinResourceImageSource(resourceName2).openSource().buffer().use {
//            it.readByteArray().decodeToString()
//        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val resourceName1 = ResourceImages.cat.resourceName
        val resourceName2 = ResourceImages.dog.resourceName

        val source1 = KotlinResourceImageSource(resourceName1)
        val source12 = KotlinResourceImageSource(resourceName1)
        val source2 = KotlinResourceImageSource(resourceName2)
        val source22 = KotlinResourceImageSource(resourceName2)

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
        val resourceName1 = ResourceImages.cat.resourceName
        val resourceName2 = ResourceImages.dog.resourceName

        assertEquals(
            expected = "KotlinResourceImageSource('$resourceName1')",
            actual = KotlinResourceImageSource(resourceName1).toString()
        )
        assertEquals(
            expected = "KotlinResourceImageSource('$resourceName2')",
            actual = KotlinResourceImageSource(resourceName2).toString()
        )
    }
}