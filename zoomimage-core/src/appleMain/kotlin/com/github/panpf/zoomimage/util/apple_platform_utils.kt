/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 * Copyright (C) 2026 Kuki93
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

@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package com.github.panpf.zoomimage.util

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.darwin.ByteVar
import platform.posix.memcpy

/**
 * Convert a ByteArray to NSData by pinning the byte array and creating an NSData object that references the pinned memory.
 *
 * @see com.github.panpf.zoomimage.core.apple.test.util.ApplePlatformUtilsTest.testByteArrayToNSData
 */
internal fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

/**
 * Convert an NSData to ByteArray by creating a new ByteArray of the appropriate size and copying the bytes from the NSData into it using memcpy.
 *
 * @see com.github.panpf.zoomimage.core.apple.test.util.ApplePlatformUtilsTest.testNSDataToByteArray
 */
internal fun NSData.toByteArray(): ByteArray {
    val byteArray = ByteArray(length.toInt())
    val byteVars = bytes?.reinterpret<ByteVar>()
    if (byteVars != null) {
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), byteVars, length)
        }
    }
    return byteArray
}
