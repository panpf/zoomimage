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

package com.github.panpf.zoomimage

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.getRequestFromView
import com.bumptech.glide.internalModel
import com.bumptech.glide.request.SingleRequest
import com.github.panpf.zoomimage.glide.GlideModelToImageSource
import com.github.panpf.zoomimage.glide.GlideModelToImageSourceImpl
import com.github.panpf.zoomimage.glide.GlideTileImageCache
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.launch

/**
 * An ImageView that integrates the Glide image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val glideZoomImageView = GlideZoomImageView(context)
 * Glide.with(this@GlideZoomImageViewFragment)
 *     .load("https://sample.com/sample.jpeg")
 *     .placeholder(R.drawable.placeholder)
 *     .into(glideZoomImageView)
 * ```
 *
 * @see com.github.panpf.zoomimage.view.glide.test.GlideZoomImageViewTest
 */
open class GlideZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    private val convertors = mutableListOf<GlideModelToImageSource>()
    private var resetImageSourceOnAttachedToWindow: Boolean = false

    fun registerModelToImageSource(convertor: GlideModelToImageSource) {
        convertors.add(0, convertor)
    }

    fun unregisterModelToImageSource(convertor: GlideModelToImageSource) {
        convertors.remove(convertor)
    }

    override fun newLogger(): Logger = Logger(tag = "GlideZoomImageView")

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        if (isAttachedToWindow) {
            resetImageSource()
        } else {
            resetImageSourceOnAttachedToWindow = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (resetImageSourceOnAttachedToWindow) {
            resetImageSourceOnAttachedToWindow = false
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        // You must use post to delay execution because 'request.isComplete' may not be ready when onDrawableChanged is executed.
        post {
            if (!isAttachedToWindow) {
                resetImageSourceOnAttachedToWindow = true
                return@post
            }
            val coroutineScope = coroutineScope!!
            val request = getRequestFromView(this@GlideZoomImageView)
            _subsamplingEngine?.apply {
                // In order to be consistent with other ZoomImageViews, TileImageCache is also configured here,
                // although it can be set in the constructor
                if (tileImageCacheState.value == null) {
                    tileImageCacheState.value = GlideTileImageCache(Glide.get(context))
                }
                coroutineScope.launch {
                    setImageSource(newImageSource(request))
                }
            }
        }
    }

    private suspend fun newImageSource(request: SingleRequest<*>?): ImageSource.Factory? {
        if (request == null) {
            logger.d { "GlideZoomImageView. Can't use Subsampling, request is null" }
            return null
        }
        if (!request.isComplete) {
            logger.d { "GlideZoomImageView. Can't use Subsampling, request is not complete" }
            return null
        }
        val model: Any? = request.internalModel
        if (model == null) {
            logger.d { "GlideZoomImageView. Can't use Subsampling, model is null" }
            return null
        }
        val imageSource = convertors.plus(GlideModelToImageSourceImpl())
            .firstNotNullOfOrNull {
                it.modelToImageSource(context, Glide.get(context), model)
            }
        if (imageSource == null) {
            logger.w { "GlideZoomImageView. Can't use Subsampling, unsupported model: '$model'" }
        }
        return imageSource
    }
}