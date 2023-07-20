/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
import com.bumptech.glide.load.engine.getModel
import com.bumptech.glide.load.engine.requestOptionsCompat
import com.bumptech.glide.request.SingleRequest
import com.github.panpf.zoomimage.glide.internal.GlideTileBitmapPool
import com.github.panpf.zoomimage.glide.internal.GlideTileMemoryCache
import com.github.panpf.zoomimage.glide.internal.newGlideImageSource

open class GlideZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    init {
        val glide = Glide.get(context)
        _subsamplingAbility?.tileBitmapPool = GlideTileBitmapPool(glide)
        _subsamplingAbility?.tileMemoryCache = GlideTileMemoryCache(glide)
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
            _subsamplingAbility?.disableMemoryCache = isDisableMemoryCache(request)
            val model = request.getModel()
            val imageSource = newGlideImageSource(context, model)
            if (imageSource == null) {
                logger.w { "GlideZoomImageView. Can't use Subsampling, unsupported model: '$model'" }
            }
            _subsamplingAbility?.setImageSource(imageSource)
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun isDisableMemoryCache(request: SingleRequest<*>): Boolean {
        val requestOptions = request.requestOptionsCompat
        return !requestOptions.isMemoryCacheable()
    }
}