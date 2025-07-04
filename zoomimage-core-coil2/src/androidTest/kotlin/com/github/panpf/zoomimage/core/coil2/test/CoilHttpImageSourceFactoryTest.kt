package com.github.panpf.zoomimage.core.coil2.test

import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.request.ImageRequest
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CoilHttpImageSourceFactoryTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()
        val imageUri = "https://www.example.com/image.jpg"
        val request = ImageRequest.Builder(context).data(imageUri).build()

        CoilHttpImageSource.Factory(context, imageLoader, request)
        CoilHttpImageSource.Factory(context, imageLoader, imageUri)
    }

    @Test
    fun testKeyAndUrl() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()
        val imageUri = "https://www.example.com/image.jpg"
        val request = ImageRequest.Builder(context).data(imageUri).build()

        val factory = CoilHttpImageSource.Factory(context, imageLoader, request)
        assertEquals(request.data.toString(), factory.key)
        assertEquals(request.data.toString(), factory.url)
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()

        val request1 =
            ImageRequest.Builder(context).data("https://www.example.com/image1.jpg").build()
        val request2 =
            ImageRequest.Builder(context).data("https://www.example.com/image2.jpg").build()

        val element1 = CoilHttpImageSource.Factory(context, imageLoader, request1)
        val element11 = CoilHttpImageSource.Factory(context, imageLoader, request1)
        val element2 = CoilHttpImageSource.Factory(context, imageLoader, request2)

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
    }

    @Test
    fun testToString() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()
        val request =
            ImageRequest.Builder(context).data("https://www.example.com/image1.jpg").build()

        val factory = CoilHttpImageSource.Factory(context, imageLoader, request)
        assertEquals("CoilHttpImageSource.Factory($request)", factory.toString())
    }
}