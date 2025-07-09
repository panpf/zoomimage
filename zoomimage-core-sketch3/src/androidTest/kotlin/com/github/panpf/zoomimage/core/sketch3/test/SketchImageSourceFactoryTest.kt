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

import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.AssetDataSource
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.zoomimage.sketch.SketchImageSource
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SketchImageSourceFactoryTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = ResourceImages.dog.uri

        val request = LoadRequest(context, imageUri)
        SketchImageSource.Factory(sketch, request)

        SketchImageSource.Factory(sketch, imageUri)
    }

    @Test
    fun testKeyAndImageUri() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = ResourceImages.dog.uri
        val request = LoadRequest(context, imageUri)
        val factory = SketchImageSource.Factory(sketch, request)
        assertEquals(request.uriString, factory.key)
        assertEquals(request.uriString, factory.imageUri)
    }

    @Test
    fun testCreate() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        SketchImageSource.Factory(
            sketch = sketch,
            request = LoadRequest(context, newAssetUri(ResourceImages.dog.resourceName))
        ).let { runBlocking { it.create() } }.apply {
            assertTrue(
                this.dataSource is AssetDataSource,
                message = "${this.dataSource}"
            )
            assertEquals(DataFrom.LOCAL, this.dataSource.dataFrom)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val request1 = LoadRequest(context, ResourceImages.dog.uri)
        val request2 = LoadRequest(context, ResourceImages.cat.uri)

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
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = ResourceImages.dog.uri
        val request = LoadRequest(context, imageUri)

        val factory = SketchImageSource.Factory(sketch, request)
        assertEquals("SketchImageSource.Factory(${request.uriString})", factory.toString())
    }
}