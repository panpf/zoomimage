package com.github.panpf.zoomimage.core.sketch4.desktop.test

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.sketch.source.FileDataSource
import com.github.panpf.sketch.source.KotlinResourceDataSource
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import okio.buffer
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SketchImageSourceTest {

    @Test
    fun testConstructor() {
        val imageUri = "/sdcard/sample.jpeg"
        val dataSource = FileDataSource(
            path = imageUri.toPath(),
            dataFrom = DataFrom.RESULT_CACHE
        )

        SketchImageSource(imageUri, dataSource)
    }

    @Test
    fun testKey() {
        val imageUri = "/sdcard/sample.jpeg"
        val dataSource = FileDataSource(
            path = imageUri.toPath(),
            dataFrom = DataFrom.RESULT_CACHE
        )
        val imageSource = SketchImageSource(imageUri, dataSource)
        assertEquals(imageUri, imageSource.key)
    }

    @Test
    fun testOpenSource() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch(context)
        SketchImageSource.Factory(
            sketch = sketch,
            request = ImageRequest(context, ResourceImages.dog.uri)
        ).let { runBlocking { it.create() } }.apply {
            assertTrue(
                this.dataSource is KotlinResourceDataSource,
                message = "${this.dataSource}"
            )
            assertEquals(DataFrom.LOCAL, this.dataSource.dataFrom)
            val imageSize = this.openSource()
                .buffer().use { it.readByteArray() }
                .let { Image.makeFromEncoded(it) }
                .let { IntSizeCompat(it.width, it.height) }
            assertEquals(expected = IntSizeCompat(1100, 733), actual = imageSize)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val imageUri1 = "/sdcard/sample.jpeg"
        val dataSource1 = FileDataSource(
            path = imageUri1.toPath(),
            dataFrom = DataFrom.RESULT_CACHE
        )
        val imageUri2 = "/sdcard/sample.png"
        val dataSource2 = FileDataSource(
            path = imageUri2.toPath(),
            dataFrom = DataFrom.RESULT_CACHE
        )

        val element1 = SketchImageSource(imageUri1, dataSource1)
        val element11 = SketchImageSource(imageUri1, dataSource1)
        val element2 = SketchImageSource(imageUri2, dataSource1)
        val element3 = SketchImageSource(imageUri1, dataSource2)

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
        val imageUri = "/sdcard/sample.jpeg"
        val dataSource = FileDataSource(
            path = imageUri.toPath(),
            dataFrom = DataFrom.RESULT_CACHE
        )
        val imageSource = SketchImageSource(imageUri, dataSource)

        assertEquals(
            expected = "SketchImageSource('$imageUri')",
            actual = imageSource.toString()
        )
    }
}