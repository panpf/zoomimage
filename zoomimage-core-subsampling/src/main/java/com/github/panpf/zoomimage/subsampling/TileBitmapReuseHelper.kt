/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.subsampling

/**
 * Assist [TileDecoder] to obtain Bitmap from [TileBitmapPool] and set it to BitmapFactory and release Bitmap
 *
 * @see [com.github.panpf.zoomimage.core.test.subsampling.TileBitmapPoolHelperTest]
 */
interface TileBitmapReuseHelper {

    val spec: TileBitmapReuseSpec

    fun free(tileBitmap: TileBitmap?, caller: String)
}