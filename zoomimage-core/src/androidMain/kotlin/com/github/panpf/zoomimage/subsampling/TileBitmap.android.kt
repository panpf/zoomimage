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
 * Bitmap, which is a alias of [android.graphics.Bitmap]
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testBitmapTypealias
 */
actual typealias TileBitmap = android.graphics.Bitmap

/**
 * Get the width of the bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testWidth
 */
@Suppress("ConflictingExtensionProperty")
actual val TileBitmap.width: Int
    get() = this.width

/**
 * Get the height of the bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testHeight
 */
@Suppress("ConflictingExtensionProperty")
actual val TileBitmap.height: Int
    get() = this.height

/**
 * Returns the minimum number of bytes that can be used to store this bitmap's pixels.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testByteCount
 */
@Suppress("ConflictingExtensionProperty")
actual val TileBitmap.byteCount: Long
    get() = this.byteCount.toLong()

/**
 * Returns true if the bitmap is mutable
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testIsMutable
 */
@Suppress("ConflictingExtensionProperty")
actual val TileBitmap.isMutable: Boolean
    get() = this.isMutable

/**
 * Returns true if the bitmap is immutable
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testIsImmutable
 */
actual val TileBitmap.isImmutable: Boolean
    get() = !this.isMutable

/**
 * Returns true if the bitmap is recycled
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testIsRecycled
 */
@Suppress("ConflictingExtensionProperty")
actual val TileBitmap.isRecycled: Boolean
    get() = this.isRecycled

/**
 * Recycle the bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testRecycle
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun TileBitmap.recycle() {
    this.recycle()
}

/**
 * Convert SkiaBitmap to a log string
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testToLogString
 */
actual fun TileBitmap.toLogString(): String = toLogString()
