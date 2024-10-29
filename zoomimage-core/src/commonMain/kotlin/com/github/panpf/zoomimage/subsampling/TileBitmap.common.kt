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

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.IntSizeCompat

/**
 * Bitmap, which is a alias of platform Bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testBitmapTypealias
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testBitmapTypealias
 */
expect class TileBitmap

/**
 * Get the width of the bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testWidth
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testWidth
 */
expect val TileBitmap.width: Int

/**
 * Get the height of the bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testHeight
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testHeight
 */
expect val TileBitmap.height: Int

/**
 * Get image size
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testSize
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testSize
 */
val TileBitmap.size: IntSizeCompat
    get() = IntSizeCompat(width, height)

/**
 * Returns the minimum number of bytes that can be used to store this bitmap's pixels.
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testByteCount
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testByteCount
 */
expect val TileBitmap.byteCount: Long

/**
 * Returns true if the bitmap is mutable
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testIsMutable
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testIsMutable
 */
expect val TileBitmap.isMutable: Boolean

/**
 * Returns true if the bitmap is immutable
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testIsImmutable
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testIsImmutable
 */
expect val TileBitmap.isImmutable: Boolean

/**
 * Returns true if the bitmap is recycled
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testIsRecycled
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testIsRecycled
 */
expect val TileBitmap.isRecycled: Boolean

/**
 * Recycle the bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.subsampling.TileBitmapAndroidTest.testRecycle
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.TileBitmapNonAndroidTest.testRecycle
 */
expect fun TileBitmap.recycle()

/**
 * Convert SkiaBitmap to a log string
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testToLogString
 * @see com.github.panpf.zoomimage.core.nonandroid.test.util.CoreUtilsNonAndroidTest.testToLogString
 */
expect fun TileBitmap.toLogString(): String