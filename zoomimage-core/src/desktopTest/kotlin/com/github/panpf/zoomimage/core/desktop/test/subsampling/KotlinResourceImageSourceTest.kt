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

package com.github.panpf.zoomimage.core.desktop.test.subsampling

import com.github.panpf.zoomimage.images.KotlinResImageFiles
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.KotlinResourceImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class KotlinResourceImageSourceTest {

    @Test
    fun testFromKotlinResource() = runTest {
        val resourceName = KotlinResImageFiles.cat.name

        assertEquals(
            expected = KotlinResourceImageSource(resourceName),
            actual = ImageSource.fromKotlinResource(resourceName)
        )

        assertNotEquals(
            illegal = KotlinResourceImageSource(resourceName),
            actual = ImageSource.fromKotlinResource("${resourceName}_fake")
        )
    }

    @Test
    fun testKey() = runTest {
        val resourceName = KotlinResImageFiles.cat.name

        assertEquals(
            expected = "file:///kotlin_resource/$resourceName",
            actual = KotlinResourceImageSource(resourceName).key
        )
    }

    @Test
    fun testOpenSource() = runTest {
        val resourceName = KotlinResImageFiles.cat.name
        KotlinResourceImageSource(resourceName).openSource().buffer().use {
            it.readByteArray()
        }
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val resourceName = KotlinResImageFiles.cat.name
        val resourceName2 = "${resourceName}_fake"

        val source1 = KotlinResourceImageSource(resourceName)
        val source12 = KotlinResourceImageSource(resourceName)
        val source2 = KotlinResourceImageSource(resourceName2)
        val source22 = KotlinResourceImageSource(resourceName2)

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
        val resourceName = KotlinResImageFiles.cat.name

        assertEquals(
            expected = "KotlinResourceImageSource('$resourceName')",
            actual = KotlinResourceImageSource(resourceName).toString()
        )
    }
}