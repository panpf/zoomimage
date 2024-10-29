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

package com.github.panpf.zoomimage.util

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Rect

/**
 * Convert [IntRectCompat] to [Rect]
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.util.CoreUtilsNonAndroidTest.testToSkiaRect
 */
internal fun IntRectCompat.toSkiaRect(): Rect = Rect(
    left = left.toFloat(),
    top = top.toFloat(),
    right = right.toFloat(),
    bottom = bottom.toFloat(),
)

/**
 * Convert SkiaBitmap to a log string
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.util.CoreUtilsNonAndroidTest.testToLogString
 */
internal fun Bitmap.toLogString(): String =
    "Bitmap@${hashCode().toString(16)}(${width.toFloat()}x${height.toFloat()},${colorType},${colorSpace})"