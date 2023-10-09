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

package com.github.panpf.zoomimage

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.internalModel
import com.bumptech.glide.load.engine.internalRequestOptions
import com.bumptech.glide.request.SingleRequest
import com.github.panpf.zoomimage.glide.GlideTileBitmapPool
import com.github.panpf.zoomimage.glide.GlideTileMemoryCache
import com.github.panpf.zoomimage.glide.newGlideImageSource

/**
 * An ImageView that integrates the Glide image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val glideZoomImageView = GlideZoomImageView(context)
 * Glide.with(this@GlideZoomImageViewFragment)
 *     .load("http://sample.com/sample.jpg")
 *     .placeholder(R.drawable.placeholder)
 *     .into(glideZoomImageView)
 * ```
 */
open class GlideZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    init {
        val glide = Glide.get(context)
        _subsamplingEngine?.tileBitmapPoolState?.value = GlideTileBitmapPool(glide)
        _subsamplingEngine?.tileMemoryCacheState?.value = GlideTileMemoryCache(glide)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (drawable != null) {
            resetImageSource()
        }
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        if (ViewCompat.isAttachedToWindow(this)) {
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        post {
            if (!ViewCompat.isAttachedToWindow(this)) {
                return@post
            }
            val request = getTag(com.bumptech.glide.R.id.glide_custom_view_target_tag)
            if (request == null) {
                logger.d { "GlideZoomImageView. Can't use Subsampling, request is null" }
                return@post
            }
            if (request !is SingleRequest<*>) {
                logger.d { "GlideZoomImageView. Can't use Subsampling, request is not SingleRequest" }
                return@post
            }
            if (!request.isComplete) {
                logger.d { "GlideZoomImageView. Can't use Subsampling, request is not complete" }
                return@post
            }
            _subsamplingEngine?.disableMemoryCacheState?.value = isDisableMemoryCache(request)
            val model = request.internalModel
            val imageSource = newGlideImageSource(context, model)
            if (imageSource == null) {
                logger.w { "GlideZoomImageView. Can't use Subsampling, unsupported model: '$model'" }
            }
            _subsamplingEngine?.setImageSource(imageSource)
        }
    }

    private fun isDisableMemoryCache(request: SingleRequest<*>): Boolean {
        val requestOptions = request.internalRequestOptions
        return !requestOptions.isMemoryCacheable
    }
}