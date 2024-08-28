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

@file:Suppress("PackageDirectoryMismatch")

package com.squareup.picasso

/**
 * @see com.github.panpf.zoomimage.core.picasso.test.internal.PicassoTest.testCheckMemoryCacheDisabled
 */
@Deprecated("No use anymore")
internal fun checkMemoryCacheDisabled(memoryPolicy: Int): Boolean {
    return !MemoryPolicy.shouldReadFromMemoryCache(memoryPolicy)
            || !MemoryPolicy.shouldWriteToMemoryCache(memoryPolicy)
}

/**
 * @see com.github.panpf.zoomimage.core.picasso.test.internal.PicassoTest.testInternalMemoryPolicy
 */
@Deprecated("No use anymore")
internal val RequestCreator.internalMemoryPolicy: Int
    get() = try {
        this.javaClass.getDeclaredField("memoryPolicy").apply {
            isAccessible = true
        }.getInt(this)
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

/**
 * @see com.github.panpf.zoomimage.core.picasso.test.internal.PicassoTest.testInternalDownloader
 */
internal val Picasso.internalDownloader: Downloader
    get() = dispatcher.downloader

/**
 * @see com.github.panpf.zoomimage.core.picasso.test.internal.PicassoTest.testInternalCache
 */
internal val Picasso.internalCache: Cache
    get() = dispatcher.cache

/**
 * @see com.github.panpf.zoomimage.core.picasso.test.internal.PicassoTest.testInternalIndex
 */
internal val MemoryPolicy.internalIndex: Int
    get() = index