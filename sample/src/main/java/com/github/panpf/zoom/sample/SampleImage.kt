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
package com.github.panpf.zoom.sample

import com.github.panpf.sketch.fetch.newAssetUri
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.sketch.util.Size

class SampleImage(val name: String, val uri: String, val size: Size) {

    companion object {
        val DOG_HOR_RES = SampleImage("DOG_HOR", newResourceUri(R.drawable.dog_hor), Size(640, 427))
        val DOG_VER_RES = SampleImage("DOG_VER", newResourceUri(R.drawable.dog_ver), Size(150, 266))
        val DOG_HOR_ASSET = SampleImage("DOG_HOR", newAssetUri("sample_dog_hor.jpg"), Size(640, 427))
        val DOG_VER_ASSET = SampleImage("DOG_VER", newAssetUri("sample_dog_ver.jpg"), Size(150, 266))
        val WORLD_HUGE_ASSET = SampleImage("WORLD", newAssetUri("sample_huge_world.jpg"), Size(9798, 6988))
        val CARD_HUGE_ASSET = SampleImage("CARD", newAssetUri("sample_huge_card.png"), Size(7557, 5669))
        val QMSHT_HUGE_ASSET = SampleImage("QMSHT", newAssetUri("sample_long_qmsht.jpg"), Size(30000, 926))
        val COMIC_HUGE_ASSET = SampleImage("COMIC", newAssetUri("sample_long_comic.jpg"), Size(690, 12176))

        val HUGES = listOf(WORLD_HUGE_ASSET, CARD_HUGE_ASSET, QMSHT_HUGE_ASSET, COMIC_HUGE_ASSET)
        val DOGS = listOf(DOG_HOR_ASSET, DOG_VER_ASSET)
    }
}