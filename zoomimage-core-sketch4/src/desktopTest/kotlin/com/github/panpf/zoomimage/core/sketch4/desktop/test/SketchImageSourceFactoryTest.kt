package com.github.panpf.zoomimage.core.sketch4.desktop.test

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.sketch.source.KotlinResourceDataSource
import com.github.panpf.zoomimage.sketch.SketchImageSource
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SketchImageSourceFactoryTest {

    @Test
    fun testConstructor() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch(context)
        val imageUri = ResourceImages.dog.uri

        val request = ImageRequest(context, imageUri)
        SketchImageSource.Factory(sketch, request)

        SketchImageSource.Factory(sketch, imageUri)
    }

    @Test
    fun testKeyAndImageUri() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch(context)
        val request = ImageRequest(context, ResourceImages.dog.uri)
        val factory = SketchImageSource.Factory(sketch, request)
        assertEquals(request.key, factory.key)
        assertEquals(request.uri.toString(), factory.imageUri)
    }

    @Test
    fun testCreate() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch(context)
        sketch.downloadCache.clear()
        SketchImageSource.Factory(
            sketch = sketch,
            request = ImageRequest(context, ResourceImages.dog.uri)
        ).let { runBlocking { it.create() } }.apply {
            assertTrue(
                this.dataSource is KotlinResourceDataSource,
                message = "${this.dataSource}"
            )
            assertEquals(DataFrom.LOCAL, this.dataSource.dataFrom)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch(context)
        val request1 = ImageRequest(context, ResourceImages.dog.uri)
        val request2 = ImageRequest(context, ResourceImages.cat.uri)

        val element1 = SketchImageSource.Factory(sketch, request1)
        val element11 = SketchImageSource.Factory(sketch, request1)
        val element2 = SketchImageSource.Factory(sketch, request2)

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
    }

    @Test
    fun testToString() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch(context)
        val imageUri = ResourceImages.dog.uri
        val request = ImageRequest(context, imageUri)

        val factory = SketchImageSource.Factory(sketch, request)
        assertEquals("SketchImageSource.Factory($request)", factory.toString())
    }
}