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
 * Give org.jetbrains.skia.Bitmap an alias called SkiaBitmap
 */
typealias SkiaBitmap = org.jetbrains.skia.Bitmap

/**
 * Give org.jetbrains.skia.Canvas an alias called SkiaCanvas
 */
typealias SkiaCanvas = org.jetbrains.skia.Canvas

/**
 * Give org.jetbrains.skia.Rect an alias called SkiaRect
 */
typealias SkiaRect = org.jetbrains.skia.Rect

/**
 * Give org.jetbrains.skia.Image an alias called SkiaImage
 */
typealias SkiaImage = org.jetbrains.skia.Image



/**
 * Convert SkiaBitmap to a log string
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.SkiasTest.testToLogString
 */
internal fun SkiaBitmap.toLogString(): String =
    "SkiaBitmap@${hashCode().toString(16)}(${width.toFloat()}x${height.toFloat()},${colorType})"