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
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.AssetDataSource
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.datasource.FileDataSource
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.zoomimage.images.AssetImageFiles
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.buffer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SketchImageSourceTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = "/sdcard/sample.jpeg"
        val request = LoadRequest(context, imageUri)
        val dataSource = FileDataSource(
            sketch = sketch,
            request = request,
            file = File(imageUri)
        )

        SketchImageSource(imageUri, dataSource)
    }

    @Test
    fun testKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = "/sdcard/sample.jpeg"
        val request = LoadRequest(context, imageUri)
        val dataSource = FileDataSource(
            sketch = sketch,
            request = request,
            file = File(imageUri)
        )
        val imageSource = SketchImageSource(imageUri, dataSource)
        assertEquals(imageUri, imageSource.key)
    }

    @Test
    fun testOpenSource() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        SketchImageSource.Factory(
            sketch = sketch,
            request = LoadRequest(context, AssetImageFiles.dog.sketch3Uri)
        ).let { runBlocking { it.create() } }.apply {
            assertTrue(
                actual = this.dataSource is AssetDataSource,
                message = "${this.dataSource}"
            )
            assertEquals(DataFrom.LOCAL, this.dataSource.dataFrom)
            val imageSize = this.openSource()
                .buffer().use { it.readByteArray() }
                .let { BitmapFactory.decodeStream(it.inputStream()) }
                .let { IntSizeCompat(it.width, it.height) }
            assertEquals(expected = IntSizeCompat(1100, 733), actual = imageSize)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri1 = "/sdcard/sample.jpeg"
        val imageUri2 = "/sdcard/sample.png"
        val request1 = LoadRequest(context, imageUri1)
        val request2 = LoadRequest(context, imageUri2)
        val dataSource1 = FileDataSource(
            sketch = sketch,
            request = request1,
            file = File(imageUri1)
        )
        val dataSource2 = FileDataSource(
            sketch = sketch,
            request = request2,
            file = File(imageUri2)
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
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val imageUri = "/sdcard/sample.jpeg"
        val request = LoadRequest(context, imageUri)
        val dataSource = FileDataSource(
            sketch = sketch,
            request = request,
            file = File(imageUri)
        )
        val imageSource = SketchImageSource(imageUri, dataSource)

        assertEquals(
            expected = "SketchImageSource('$imageUri')",
            actual = imageSource.toString()
        )
    }
}