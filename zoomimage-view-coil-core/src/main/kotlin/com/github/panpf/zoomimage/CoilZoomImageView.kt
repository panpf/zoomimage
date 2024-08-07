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
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.SuccessResult
import coil3.transition.CrossfadeDrawable
import coil3.util.CoilUtils
import com.github.panpf.zoomimage.coil.CoilModelToImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.coil.CoilTileBitmapCache
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.view.coil.internal.getImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
 * @see com.github.panpf.zoomimage.view.coil.core.test.CoilZoomImageViewTest
 */
open class CoilZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    private val convertors = mutableListOf<CoilModelToImageSource>()
    private var coroutineScope: CoroutineScope? = null

    fun registerModelToImageSource(convertor: CoilModelToImageSource) {
        convertors.add(0, convertor)
    }

    fun unregisterModelToImageSource(convertor: CoilModelToImageSource) {
        convertors.remove(convertor)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        if (drawable != null) {
            resetImageSource()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope?.cancel("onDetachedFromWindow")
        coroutineScope = null
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        if (isAttachedToWindow) {
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        post {
            if (!isAttachedToWindow) {
                return@post
            }
            val coroutineScope = coroutineScope ?: return@post
            val result = CoilUtils.result(this)
            if (result == null) {
                logger.d { "CoilZoomImageView. Can't use Subsampling, result is null" }
                return@post
            }
            if (result !is SuccessResult) {
                logger.d { "CoilZoomImageView. Can't use Subsampling, result is not Success" }
                return@post
            }
            val imageLoader = CoilUtils.getImageLoader(this@CoilZoomImageView)
            if (imageLoader == null) {
                logger.d { "CoilZoomImageView. Can't use Subsampling, ImageLoader is null" }
                return@post
            }
            _subsamplingEngine?.apply {
                if (tileBitmapCacheState.value == null) {
                    tileBitmapCacheState.value = CoilTileBitmapCache(imageLoader)
                }
                disabledTileBitmapCacheState.value = isDisallowMemoryCache(result)
                coroutineScope.launch {
                    setImageSource(newImageSource(imageLoader, result))
                }
            }
        }
    }

    private fun isDisallowMemoryCache(result: SuccessResult): Boolean {
        return result.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private suspend fun newImageSource(
        imageLoader: ImageLoader,
        result: SuccessResult
    ): ImageSource.Factory? {
        val drawable = drawable
        if (drawable == null) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, drawable is null" }
            return null
        }
        val lastChildDrawable = drawable.getLastChildDrawable()
        if (lastChildDrawable !is BitmapDrawable) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, drawable is not BitmapDrawable" }
            return null
        }
        if (lastChildDrawable is Animatable) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, drawable is Animatable" }
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

    private fun Drawable.getLastChildDrawable(): Drawable? {
        return when (val drawable = this) {
            is CrossfadeDrawable -> {
                drawable.end?.getLastChildDrawable()
            }

            is LayerDrawable -> {
                val layerCount = drawable.numberOfLayers.takeIf { it > 0 } ?: return null
                drawable.getDrawable(layerCount - 1).getLastChildDrawable()
            }

            else -> drawable
        }
    }

    override fun getLogTag(): String? = "CoilZoomImageView"
}