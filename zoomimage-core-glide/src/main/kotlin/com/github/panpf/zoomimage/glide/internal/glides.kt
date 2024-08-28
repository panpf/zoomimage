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

package com.bumptech.glide

import android.widget.ImageView
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.SingleRequest

/**
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlidesTest.testSingleRequestInternalRequestOptions
 */
@Deprecated("No use anymore")
internal val SingleRequest<*>.internalRequestOptions: BaseRequestOptions<*>
    get() = this.javaClass.getDeclaredField("requestOptions")
        .apply { isAccessible = true }
        .get(this) as BaseRequestOptions<*>

/**
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlidesTest.testSingleRequestInternalModel
 */
val SingleRequest<*>.internalModel: Any?
    get() {
        return try {
            this.javaClass.getDeclaredField("model").apply {
                isAccessible = true
            }.get(this)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

/**
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlidesTest.testRequestBuilderInternalModel
 */
val RequestBuilder<*>.internalModel: Any?
    get() {
        return try {
            this.javaClass.getDeclaredField("model").apply {
                isAccessible = true
            }.get(this)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

/**
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlidesTest.testInternalGlideContext
 */
val Glide.internalGlideContext: GlideContext
    get() = glideContext

fun getRequestFromView(view: ImageView): SingleRequest<*>? {
    @Suppress("RemoveRedundantQualifierName")
    return view.getTag(com.bumptech.glide.R.id.glide_custom_view_target_tag) as? SingleRequest<*>
}