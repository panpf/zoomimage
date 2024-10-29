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
import coil.ImageLoader
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.util.CoilUtils
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.coil.CoilTileImageCache
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.view.coil.internal.getImageLoader
import kotlinx.coroutines.launch

/**
 * An ImageView that integrates the Coil image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val coilZoomImageView = CoilZoomImageView(context)
 * coilZoomImageView.load("https://sample.com/sample.jpeg") {
 *     placeholder(R.drawable.placeholder)
 *     crossfade(true)
 * }
 * ```
 *
 * @see com.github.panpf.zoomimage.view.coil2.core.test.CoilZoomImageViewTest
 */
open class CoilZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    private val convertors = mutableListOf<CoilModelToImageSource>()
    private var resetImageSourceOnAttachedToWindow: Boolean = false

    fun registerModelToImageSource(convertor: CoilModelToImageSource) {
        convertors.add(0, convertor)
    }

    fun unregisterModelToImageSource(convertor: CoilModelToImageSource) {
        convertors.remove(convertor)
    }

    override fun newLogger(): Logger = Logger(tag = "CoilZoomImageView")

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
        // You must use post to delay execution because 'CoilUtils.result' may not be ready when onDrawableChanged is executed.
        post {
            if (!isAttachedToWindow) {
                resetImageSourceOnAttachedToWindow = true
                return@post
            }
            val coroutineScope = coroutineScope!!
            val imageLoader = CoilUtils.getImageLoader(this@CoilZoomImageView)
            val result = CoilUtils.result(this)
            _subsamplingEngine?.apply {
                if (tileImageCacheState.value == null && imageLoader != null) {
                    tileImageCacheState.value = CoilTileImageCache(imageLoader)
                }
                coroutineScope.launch {
                    setImageSource(newImageSource(imageLoader, result))
                }
            }
        }
    }

    private suspend fun newImageSource(
        imageLoader: ImageLoader?,
        result: ImageResult?
    ): ImageSource.Factory? {
        val drawable = drawable
        if (drawable == null) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, drawable is null" }
            return null
        }
        if (imageLoader == null) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, imageLoader is null" }
            return null
        }
        if (result !is SuccessResult) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, result is not Success" }
            return null
        }
        val model = result.request.data
        val imageSource = convertors.plus(CoilModelToImageSourceImpl())
            .firstNotNullOfOrNull {
                it.modelToImageSource(context, imageLoader, model)
            }
        if (imageSource == null) {
            logger.w { "GlideZoomImageView. Can't use Subsampling, unsupported model: '$model'" }
        }
        return imageSource
    }
}