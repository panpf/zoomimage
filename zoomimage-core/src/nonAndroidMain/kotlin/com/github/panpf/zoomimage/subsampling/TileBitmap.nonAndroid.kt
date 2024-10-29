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

@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.toLogString

/**
 * Bitmap, which is a alias of [org.jetbrains.skia.Bitmap]
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testBitmapTypealias
 */
actual typealias TileBitmap = org.jetbrains.skia.Bitmap

/**
 * Get the width of the bitmap
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testWidth
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val TileBitmap.width: Int
    get() = this.width

/**
 * Get the height of the bitmap
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testHeight
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val TileBitmap.height: Int
    get() = this.height

/**
 * Returns the minimum number of bytes that can be used to store this bitmap's pixels.
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testByteCount
 */
actual val TileBitmap.byteCount: Long
    get() = (rowBytes * height).toLong()

/**
 * Returns true if the bitmap is mutable
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testIsMutable
 */
actual val TileBitmap.isMutable: Boolean
    get() = !this.isImmutable

/**
 * Returns true if the bitmap is immutable
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testIsImmutable
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val TileBitmap.isImmutable: Boolean
    get() = this.isImmutable

/**
 * Returns true if the bitmap is recycled
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testIsRecycled
 */
actual val TileBitmap.isRecycled: Boolean
    get() = false

/**
 * Recycle the bitmap
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testRecycle
 */
actual fun TileBitmap.recycle() {

}

/**
 * Convert SkiaBitmap to a log string
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.util.CoreUtilsNonAndroidTest.testToLogString
 */
actual fun TileBitmap.toLogString(): String = toLogString()