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

import android.graphics.Bitmap
import android.os.Build
import android.os.Looper

/**
 * Check if the current thread is the UI thread
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testRequiredMainThread
 */
internal fun requiredMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "This method must be executed in the UI thread"
    }
}

/**
 * Check if the current thread is the work thread
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testRequiredWorkThread
 */
internal fun requiredWorkThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "This method must be executed in the work thread"
    }
}

/**
 * Get a short string description of the Bitmap
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testToShortString
 */
internal fun Bitmap.toShortString(): String = "(${width}x${height},$config)"

/**
 * Get the log string description of Bitmap, it additionally contains the hexadecimal string representation of the Bitmap memory address.
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testToLogString
 */
internal fun Bitmap.toLogString(): String = "Bitmap@${toHexString()}(${width}x${height},$config)"

/**
 * Gets a Bitmap.Config that is always non-null, or returns Bitmap.Config.ARGB_8888 if the original Bitmap.Config is null
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testSafeConfig
 */
@Suppress("USELESS_ELVIS")
internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888

/**
 * Check if the Bitmap.Config is Bitmap.Config.HARDWARE and the current system supports hardware acceleration
 *
 * @see com.github.panpf.zoomimage.core.android.test.util.CoreUtilsAndroidTest.testIsAndSupportHardware
 */
internal fun Bitmap.Config.isAndSupportHardware(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.HARDWARE