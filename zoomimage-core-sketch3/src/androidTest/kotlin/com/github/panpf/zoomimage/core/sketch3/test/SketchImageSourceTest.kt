/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.core.sketch3.test

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SketchImageSourceTest {

    @Test
    fun testKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = ResourceImages.dog.uri
        SketchImageSource.Factory(sketch, imageUri).apply {
            assertEquals(imageUri, key)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        val imageSource1 = runBlocking { SketchImageSource.Factory(sketch, imageUri1).create() }
        val imageSource12 = runBlocking { SketchImageSource.Factory(sketch, imageUri1).create() }
        val imageSource2 = runBlocking { SketchImageSource.Factory(sketch, imageUri2).create() }
        val imageSource22 = runBlocking { SketchImageSource.Factory(sketch, imageUri2).create() }

        assertEquals(expected = imageSource1, actual = imageSource1)
        assertEquals(expected = imageSource1, actual = imageSource12)
        assertEquals(expected = imageSource2, actual = imageSource22)
        assertNotEquals(illegal = imageSource1, actual = null as Any?)
        assertNotEquals(illegal = imageSource1, actual = Any())
        assertNotEquals(illegal = imageSource1, actual = imageSource2)
        assertNotEquals(illegal = imageSource12, actual = imageSource22)

        // Sketch's KotlinResourceDataSource does not implement equals and hashcode
        assertNotEquals(
            illegal = imageSource1.hashCode(),
            actual = imageSource12.hashCode()
        )
        assertNotEquals(
            illegal = imageSource2.hashCode(),
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
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        val imageSource1 = runBlocking { SketchImageSource.Factory(sketch, imageUri1).create() }
        val imageSource2 = runBlocking { SketchImageSource.Factory(sketch, imageUri2).create() }

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
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri =
            "https://images.unsplash.com/photo-1721340143289-94be4f77cda4?q=80&w=2832&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
        val diskCache = sketch.downloadCache

        diskCache.clear()
        assertEquals(null, diskCache[imageUri])

        val imageSourceFactory = SketchImageSource.Factory(sketch, imageUri)
        val imageSource = runBlocking {
            imageSourceFactory.create()
        }
        val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
        val bitmap = BitmapFactory.decodeStream(bytes.inputStream())
        val imageSize = bitmap.let { IntSizeCompat(it.width, it.height) }
        assertEquals(expected = IntSizeCompat(2832, 4240), actual = imageSize)
        assertNotEquals(
            illegal = null,
            actual = diskCache[imageUri]
        )
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        val imageSourceFactory1 = SketchImageSource.Factory(sketch, imageUri1)
        val imageSourceFactory12 = SketchImageSource.Factory(sketch, imageUri1)
        val imageSourceFactory2 = SketchImageSource.Factory(sketch, imageUri2)
        val imageSourceFactory22 = SketchImageSource.Factory(sketch, imageUri2)

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
        val sketch = Sketch.Builder(context).build()
        val imageUri1 = ResourceImages.dog.uri
        val imageUri2 = ResourceImages.cat.uri

        assertEquals(
            "SketchImageSource.Factory('$imageUri1')",
            SketchImageSource.Factory(sketch, imageUri1).toString()
        )
        assertEquals(
            "SketchImageSource.Factory('$imageUri2')",
            SketchImageSource.Factory(sketch, imageUri2).toString()
        )
    }
}