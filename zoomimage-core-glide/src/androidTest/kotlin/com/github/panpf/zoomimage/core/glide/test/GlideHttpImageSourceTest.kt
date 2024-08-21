package com.github.panpf.zoomimage.core.glide.test

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.internalDiskCache
import com.bumptech.glide.load.model.GlideUrl
import com.github.panpf.zoomimage.glide.GlideHttpImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GlideHttpImageSourceTest {

    @Test
    fun testKey() {
        val imageUri = GlideUrl("https://www.example.com/image.jpg")
        GlideHttpImageSource(imageUri) {
            throw UnsupportedOperationException()
        }.apply {
            assertEquals(imageUri.toString(), key)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val imageUri1 = GlideUrl("https://www.example.com/image1.jpg")
        val imageUri2 = GlideUrl("https://www.example.com/image2.jpg")

        val imageSource1 = GlideHttpImageSource(imageUri1) {
            throw UnsupportedOperationException()
        }
        val imageSource12 = GlideHttpImageSource(imageUri1) {
            throw UnsupportedOperationException()
        }
        val imageSource2 = GlideHttpImageSource(imageUri2) {
            throw UnsupportedOperationException()
        }
        val imageSource22 = GlideHttpImageSource(imageUri2) {
            throw UnsupportedOperationException()
        }

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
        val imageUri1 = GlideUrl("https://www.example.com/image1.jpg")
        val imageUri2 = GlideUrl("https://www.example.com/image2.jpg")

        assertEquals(
            "GlideHttpImageSource('$imageUri1')",
            GlideHttpImageSource(imageUri1) {
                throw UnsupportedOperationException()
            }.toString()
        )
        assertEquals(
            "GlideHttpImageSource('$imageUri2')",
            GlideHttpImageSource(imageUri2) {
                throw UnsupportedOperationException()
            }.toString()
        )
    }

    @Test
    fun testOpenSource() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val imageUri =
            GlideUrl("https://images.unsplash.com/photo-1721340143289-94be4f77cda4?q=80&w=2832&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
        val diskCache = glide.internalDiskCache!!

        diskCache.clear()
        assertEquals(null, diskCache.get(imageUri))

        val imageSourceFactory = GlideHttpImageSource.Factory(glide, imageUri)
        val imageSource = runBlocking {
            imageSourceFactory.create()
        }
        val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
        val bitmap = BitmapFactory.decodeStream(bytes.inputStream())
        val imageSize = bitmap.let { IntSizeCompat(it.width, it.height) }
        assertEquals(expected = IntSizeCompat(2832, 4240), actual = imageSize)
        assertNotEquals(
            illegal = null,
            actual = diskCache.get(imageUri)
        )
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val imageUri1 = GlideUrl("https://www.example.com/image1.jpg")
        val imageUri2 = GlideUrl("https://www.example.com/image2.jpg")

        val imageSourceFactory1 = GlideHttpImageSource.Factory(glide, imageUri1)
        val imageSourceFactory12 = GlideHttpImageSource.Factory(glide, imageUri1)
        val imageSourceFactory2 = GlideHttpImageSource.Factory(glide, imageUri2)
        val imageSourceFactory22 = GlideHttpImageSource.Factory(glide, imageUri2)

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
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val imageUri1 = GlideUrl("https://www.example.com/image1.jpg")
        val imageUri2 = GlideUrl("https://www.example.com/image2.jpg")

        assertEquals(
            "GlideHttpImageSource.Factory('$imageUri1')",
            GlideHttpImageSource.Factory(glide, imageUri1).toString()
        )
        assertEquals(
            "GlideHttpImageSource.Factory('$imageUri2')",
            GlideHttpImageSource.Factory(glide, imageUri2).toString()
        )
    }
}