package com.github.panpf.zoomimage.compose.resources.test

import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromComposeResource
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.current
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class ComposeResourceImageSourceTest {

    @Test
    fun testFromComposeResource() {
        val okResourcePath =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        assertEquals(
            expected = okResourcePath,
            actual = ImageSource.fromComposeResource(okResourcePath).resourcePath
        )

        val okResourcePath2 =
            "jar:file:/data/app/com.github.panpf.sketch4.sample-1==/base.apk!/composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        assertEquals(
            expected = okResourcePath,
            actual = ImageSource.fromComposeResource(okResourcePath2).resourcePath
        )

        val okResourcePath3 =
            "file:/Users/panpf/Workspace/zoomimage/sample/build/processedResources/desktop/main/composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        assertEquals(
            expected = okResourcePath,
            actual = ImageSource.fromComposeResource(okResourcePath3).resourcePath
        )

        val okResourcePath4 =
            "http://localhost:8080/./composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        assertEquals(
            expected = okResourcePath,
            actual = ImageSource.fromComposeResource(okResourcePath4).resourcePath
        )

        val okResourcePath5 =
            "file:///Users/panpf/Library/Developer/ CoreSimulator/Devices/F828C881-A750-432B-8210-93A84C45E/data/Containers/Bundle/Application/CBD75605-D35E-47A7-B56B-6C5690B062CC/SketchSample.app/compose-resources/composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        assertEquals(
            expected = okResourcePath,
            actual = ImageSource.fromComposeResource(okResourcePath5).resourcePath
        )

        val errorResourcePath = okResourcePath.replace("composeResources/", "composeResources1/")
        assertFailsWith(IllegalArgumentException::class) {
            ImageSource.fromComposeResource(errorResourcePath)
        }

        val errorResourcePath2 =
            okResourcePath5.replace("/composeResources/", "/composeResources1/")
        assertFailsWith(IllegalArgumentException::class) {
            ImageSource.fromComposeResource(errorResourcePath2)
        }
    }

    @Test
    fun testKey() = runTest {
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        assertEquals(
            expected = "file:///compose_resource/$resourceName1",
            actual = ComposeResourceImageSource(resourceName1, byteArrayOf()).key
        )
        assertEquals(
            expected = "file:///compose_resource/$resourceName2",
            actual = ComposeResourceImageSource(resourceName2, byteArrayOf()).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        ComposeResourceImageSource(resourceName1, byteArrayOf()).openSource().buffer().use {
            it.readByteArray()
        }

        ComposeResourceImageSource(resourceName2, byteArrayOf()).openSource().buffer().use {
            it.readByteArray().decodeToString()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        val source1 = ComposeResourceImageSource(resourceName1, byteArrayOf())
        val source12 = ComposeResourceImageSource(resourceName1, byteArrayOf())
        val source2 = ComposeResourceImageSource(resourceName2, byteArrayOf())
        val source22 = ComposeResourceImageSource(resourceName2, byteArrayOf())

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
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        assertEquals(
            expected = "ComposeResourceImageSource('$resourceName1')",
            actual = ComposeResourceImageSource(resourceName1, byteArrayOf()).toString()
        )
        assertEquals(
            expected = "ComposeResourceImageSource('$resourceName2')",
            actual = ComposeResourceImageSource(resourceName2, byteArrayOf()).toString()
        )
    }

    @Test
    fun testFactoryKey() = runTest {
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        assertEquals(
            expected = "file:///compose_resource/$resourceName1",
            actual = ComposeResourceImageSource.Factory(resourceName1).key
        )
        assertEquals(
            expected = "file:///compose_resource/$resourceName2",
            actual = ComposeResourceImageSource.Factory(resourceName2).key
        )
    }

    @Test
    fun testFactoryCreate() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        runTest {
            val resourceName1 =
                "composeResources/com.github.panpf.zoomimage.sample.test.compose/files/dog.jpg"

            ComposeResourceImageSource.Factory(resourceName1).create().openSource().buffer().use {
                it.readByteArray()
            }
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() = runTest {
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        val source1 = ComposeResourceImageSource.Factory(resourceName1)
        val source12 = ComposeResourceImageSource.Factory(resourceName1)
        val source2 = ComposeResourceImageSource.Factory(resourceName2)
        val source22 = ComposeResourceImageSource.Factory(resourceName2)

        assertEquals(expected = source1, actual = source12)
        assertEquals(expected = source2, actual = source22)
        assertNotEquals(illegal = source1, actual = source2)
        assertNotEquals(illegal = source12, actual = source22)

        assertEquals(expected = source1.hashCode(), actual = source12.hashCode())
        assertEquals(expected = source2.hashCode(), actual = source22.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source2.hashCode())
        assertNotEquals(illegal = source12.hashCode(), actual = source22.hashCode())
    }

    @Test
    fun testFactoryToString() = runTest {
        val resourceName1 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
        val resourceName2 =
            "composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_world.jpg"

        assertEquals(
            expected = "ComposeResourceImageSource.Factory('$resourceName1')",
            actual = ComposeResourceImageSource.Factory(resourceName1).toString()
        )
        assertEquals(
            expected = "ComposeResourceImageSource.Factory('$resourceName2')",
            actual = ComposeResourceImageSource.Factory(resourceName2).toString()
        )
    }
}