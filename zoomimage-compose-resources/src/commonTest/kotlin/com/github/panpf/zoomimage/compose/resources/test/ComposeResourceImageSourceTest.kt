package com.github.panpf.zoomimage.compose.resources.test

import com.githb.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.subsampling.ComposeResourceImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromComposeResource
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
            "file:/Users/panpf/Workspace/zoomimage/samples/shared/build/processedResources/desktop/main/composeResources/com.github.panpf.zoomimage.sample.resources/files/huge_china.jpg"
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
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        assertEquals(
            expected = "file:///compose_resource/$resourcePath",
            actual = ComposeResourceImageSource(resourcePath, byteArrayOf()).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        ComposeResourceImageSource(resourcePath, byteArrayOf())
            .openSource().buffer()
            .use { it.readByteArray() }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        val resourcePath2 = "${resourcePath}_fake"

        val source1 = ComposeResourceImageSource(resourcePath, byteArrayOf())
        val source12 = ComposeResourceImageSource(resourcePath, byteArrayOf())
        val source2 = ComposeResourceImageSource(resourcePath2, byteArrayOf())
        val source22 = ComposeResourceImageSource(resourcePath2, byteArrayOf())

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
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        assertEquals(
            expected = "ComposeResourceImageSource('$resourcePath')",
            actual = ComposeResourceImageSource(resourcePath, byteArrayOf()).toString()
        )
    }

    @Test
    fun testFactoryKey() = runTest {
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        assertEquals(
            expected = "file:///compose_resource/$resourcePath",
            actual = ComposeResourceImageSource.Factory(resourcePath).key
        )
    }

    @Test
    fun testFactoryCreate() = runTest {
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        ComposeResourceImageSource.Factory(resourcePath).create().openSource().buffer().use {
            it.readByteArray()
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() = runTest {
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        val resourcePath2 = "${resourcePath}_fake"

        val source1 = ComposeResourceImageSource.Factory(resourcePath)
        val source12 = ComposeResourceImageSource.Factory(resourcePath)
        val source2 = ComposeResourceImageSource.Factory(resourcePath2)
        val source22 = ComposeResourceImageSource.Factory(resourcePath2)

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
        val resourcePath = ComposeResImageFiles.cat.uri.let {
            val index = it.indexOf("composeResources")
            require(index >= 0) { "Invalid compose resource uri: $it" }
            it.substring(index)
        }
        assertEquals(
            expected = "ComposeResourceImageSource.Factory('$resourcePath')",
            actual = ComposeResourceImageSource.Factory(resourcePath).toString()
        )
    }
}