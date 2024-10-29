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

package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageSource

/**
 * Create a [DecodeHelper] instance using [ImageSource], on the non Android platform, [SkiaDecodeHelper] will be used
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testCreateDecodeHelper
 */
internal actual fun createDecodeHelper(imageSource: ImageSource): DecodeHelper {
    return SkiaDecodeHelper.Factory().create(imageSource)
}


/**
 * Checks whether the specified image type supports subsampling, on the non Android platform, it mainly depends on the types supported by Image.
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal.DecodesNonAndroidTest.testCheckSupportSubsamplingByMimeType
 */
internal actual fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean =
    !"image/gif".equals(mimeType, true)