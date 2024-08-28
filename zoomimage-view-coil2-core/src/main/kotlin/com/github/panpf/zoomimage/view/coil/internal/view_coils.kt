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

package com.github.panpf.zoomimage.view.coil.internal

import android.view.View
import coil.ImageLoader
import coil.util.CoilUtils

/**
 * Get the [ImageLoader] from the [View] that is currently loading the image.
 *
 * @see com.github.panpf.zoomimage.view.coil2.core.test.internal.ViewCoilsTest.testGetImageLoader
 */
@Suppress("UnusedReceiverParameter")
internal fun CoilUtils.getImageLoader(view: View): ImageLoader? {
    val requestManager = view.getTag(coil.base.R.id.coil_request_manager)
    if (requestManager != null) {
        try {
            val requestDelegate = requestManager.javaClass.getDeclaredField("currentRequest")
                .apply { isAccessible = true }
                .get(requestManager)
            if (requestDelegate != null) {
                val imageLoader = requestDelegate.javaClass.getDeclaredField("imageLoader")
                    .apply { isAccessible = true }
                    .get(requestDelegate)
                return imageLoader as? ImageLoader
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}