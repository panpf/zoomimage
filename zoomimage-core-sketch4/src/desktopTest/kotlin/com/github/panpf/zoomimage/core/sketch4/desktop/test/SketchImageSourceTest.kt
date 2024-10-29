package com.github.panpf.zoomimage.core.sketch4.desktop.test

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch.Builder
import com.github.panpf.zoomimage.sketch.SketchImageSource.Factory
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.buffer
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SketchImageSourceTest {

    @Test
    fun testKey() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        val imageUri = ResourceImages.dog.uri
        Factory(sketch, imageUri).apply {
            assertEquals(imageUri, key)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        val imageSource1 = runBlocking { Factory(sketch, imageUri1).create() }
        val imageSource12 = runBlocking { Factory(sketch, imageUri1).create() }
        val imageSource2 = runBlocking { Factory(sketch, imageUri2).create() }
        val imageSource22 = runBlocking { Factory(sketch, imageUri2).create() }

        assertEquals(expected = imageSource1, actual = imageSource1)
        assertEquals(expected = imageSource1, actual = imageSource12)
        assertEquals(expected = imageSource2, actual = imageSource22)
        assertNotEquals(illegal = imageSource1, actual = null as Any?)
        assertNotEquals(illegal = imageSource1, actual = Any())
        assertNotEquals(illegal = imageSource1, actual = imageSource2)
        assertNotEquals(illegal = imageSource12, actual = imageSource22)

        assertEquals(
            expected = imageSource1.hashCode(),
            actual = imageSource12.hashCode()
        )
        assertEquals(
            expected = imageSource2.hashCode(),
            actual = imageSource22.hashCode()
        )
        assertNotEquals(
            illegal = imageSource1.hashCode(),
            actual = imageSource2.hashCode()
        )
        assertNotEquals(
            illegal = imageSource12.hashCode(),
            actual = imageSource22.hashCode()
        )
    }

    @Test
    fun testToString() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        val imageSource1 = runBlocking { Factory(sketch, imageUri1).create() }
        val imageSource2 = runBlocking { Factory(sketch, imageUri2).create() }

        assertEquals(
            "SketchImageSource('$imageUri1')",
            imageSource1.toString()
        )
        assertEquals(
            "SketchImageSource('$imageUri2')",
            imageSource2.toString()
        )
    }

    @Test
    fun testOpenSource() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        val imageUri =
            "https://images.unsplash.com/photo-1721340143289-94be4f77cda4?q=80&w=2832&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
        val diskCache = sketch.downloadCache

        diskCache.clear()
        assertEquals(null, diskCache.openSnapshot(imageUri))

        val imageSourceFactory = Factory(sketch, imageUri)
        val imageSource = runBlocking {
            imageSourceFactory.create()
        }
        val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
        val imageSize = Image.makeFromEncoded(bytes).use { IntSizeCompat(it.width, it.height) }
        assertEquals(expected = IntSizeCompat(2832, 4240), actual = imageSize)
        assertNotEquals(
            illegal = null,
            actual = diskCache.openSnapshot(imageUri)?.apply { close() }
        )
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        val imageSourceFactory1 = Factory(sketch, imageUri1)
        val imageSourceFactory12 = Factory(sketch, imageUri1)
        val imageSourceFactory2 = Factory(sketch, imageUri2)
        val imageSourceFactory22 = Factory(sketch, imageUri2)

        assertEquals(expected = imageSourceFactory1, actual = imageSourceFactory12)
        assertEquals(expected = imageSourceFactory2, actual = imageSourceFactory22)
        assertNotEquals(illegal = imageSourceFactory1, actual = imageSourceFactory2)
        assertNotEquals(illegal = imageSourceFactory12, actual = imageSourceFactory22)

        assertEquals(
            expected = imageSourceFactory1.hashCode(),
            actual = imageSourceFactory12.hashCode()
        )
        assertEquals(
            expected = imageSourceFactory2.hashCode(),
            actual = imageSourceFactory22.hashCode()
        )
        assertNotEquals(
            illegal = imageSourceFactory1.hashCode(),
            actual = imageSourceFactory2.hashCode()
        )
        assertNotEquals(
            illegal = imageSourceFactory12.hashCode(),
            actual = imageSourceFactory22.hashCode()
        )
    }

    @Test
    fun testFactoryToString() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        assertEquals(
            "SketchImageSource.Factory('$imageUri1')",
            Factory(sketch, imageUri1).toString()
        )
        assertEquals(
            "SketchImageSource.Factory('$imageUri2')",
            Factory(sketch, imageUri2).toString()
        )
    }
}