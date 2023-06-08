/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.sample

import android.os.Environment
import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.util.Size

object SampleImages {
    val FETCHERS = listOf(Asset.WORLD, Content.CARD, Resource.QMSHT, Http.COMIC)

    object Asset {
        val HORDOG = SampleImage(
            name = "HORDOG",
            fileName = "sample_dog_hor.jpg",
            uri = newAssetUri("sample_dog_hor.jpg"),
            size = Size(640, 427)
        )
        val VERDOG = SampleImage(
            name = "VERDOG",
            fileName = "sample_dog_ver.jpg",
            uri = newAssetUri("sample_dog_ver.jpg"),
            size = Size(150, 266)
        )
        val WORLD = SampleImage(
            name = "WORLD",
            fileName = "sample_huge_world.jpg",
            uri = newAssetUri("sample_huge_world.jpg"),
            size = Size(9798, 6988)
        )
        val CARD = SampleImage(
            name = "CARD",
            fileName = "sample_huge_card.png",
            uri = newAssetUri("sample_huge_card.png"),
            size = Size(7557, 5669)
        )
        val QMSHT = SampleImage(
            name = "QMSHT",
            fileName = "sample_long_qmsht.jpg",
            uri = newAssetUri("sample_long_qmsht.jpg"),
            size = Size(30000, 926)
        )
        val COMIC = SampleImage(
            name = "COMIC",
            fileName = "sample_long_comic.jpg",
            uri = newAssetUri("sample_long_comic.jpg"),
            size = Size(690, 12176)
        )
        val ALL = listOf(HORDOG, VERDOG, WORLD, CARD, QMSHT, COMIC)
    }

    object Http {
        val WORLD = SampleImage(
            name = "WORLD",
            fileName = "sample_huge_world.jpg",
            uri = "http://img.panpengfei.com/sample_huge_world.jpg",
            size = Size(9798, 6988)
        )
        val CARD = SampleImage(
            name = "CARD",
            fileName = "sample_huge_card.png",
            uri = "http://img.panpengfei.com/sample_huge_card.png",
            size = Size(7557, 5669)
        )
        val QMSHT = SampleImage(
            name = "QMSHT",
            fileName = "sample_long_qmsht.jpg",
            uri = "http://img.panpengfei.com/sample_long_qmsht.jpg",
            size = Size(30000, 926)
        )
        val COMIC = SampleImage(
            name = "COMIC",
            fileName = "sample_long_comic.jpg",
            uri = "http://img.panpengfei.com/sample_long_comic.jpg",
            size = Size(690, 12176)
        )
        val ALL = listOf(WORLD, CARD, QMSHT, COMIC)
    }

    object Resource {
        val HORDOG = SampleImage(
            name = "HORDOG",
            fileName = "sample_dog_hor.jpg",
            uri = newResourceUri(R.raw.sample_dog_hor),
            size = Size(640, 427)
        )
        val VERDOG = SampleImage(
            name = "VERDOG",
            fileName = "sample_dog_ver.jpg",
            uri = newResourceUri(R.raw.sample_dog_ver),
            size = Size(150, 266)
        )
        val QMSHT = SampleImage(
            name = "WORLD",
            fileName = "sample_long_qmsht.jpg",
            uri = newResourceUri(R.raw.sample_long_qmsht),
            size = Size(30000, 926)
        )
        val ALL = listOf(HORDOG, VERDOG, QMSHT)
    }

    object Content {
        val WORLD = SampleImage(
            name = "WORLD",
            fileName = "sample_huge_world.jpg",
            uri = "file://${Environment.getExternalStorageDirectory()}/Android/data/${BuildConfig.APPLICATION_ID}/files/assets/sample_huge_world.jpg",
            size = Size(9798, 6988)
        )
        val CARD = SampleImage(
            name = "CARD",
            fileName = "sample_huge_card.png",
            uri = "file://${Environment.getExternalStorageDirectory()}/Android/data/${BuildConfig.APPLICATION_ID}/files/assets/sample_huge_card.png",
            size = Size(7557, 5669)
        )
        val QMSHT = SampleImage(
            name = "QMSHT",
            fileName = "sample_long_qmsht.jpg",
            uri = "file://${Environment.getExternalStorageDirectory()}/Android/data/${BuildConfig.APPLICATION_ID}/files/assets/sample_long_qmsht.png",
            size = Size(30000, 926)
        )
        val COMIC = SampleImage(
            name = "COMIC",
            fileName = "sample_long_comic.jpg",
            uri = "file://${Environment.getExternalStorageDirectory()}/Android/data/${BuildConfig.APPLICATION_ID}/files/assets/sample_long_comic.png",
            size = Size(690, 12176)
        )
    }
}

class SampleImage(val name: String, val fileName: String, val uri: String, val size: Size)