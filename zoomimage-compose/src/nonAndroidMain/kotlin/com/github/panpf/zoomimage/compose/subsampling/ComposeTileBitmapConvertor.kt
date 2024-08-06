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

package com.github.panpf.zoomimage.compose.subsampling

import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor

/**
 * Convert [SkiaTileBitmap] to [ComposeSkiaTileBitmap]
 *
 * @see com.github.panpf.zoomimage.compose.nonandroid.test.subsampling.ComposeTileBitmapConvertorTest
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ComposeTileBitmapConvertor : TileBitmapConvertor {

    actual override suspend fun convert(tileBitmap: TileBitmap): TileBitmap {
        val skiaTileBitmap = tileBitmap as SkiaTileBitmap
        return ComposeSkiaTileBitmap(skiaTileBitmap)
    }
}