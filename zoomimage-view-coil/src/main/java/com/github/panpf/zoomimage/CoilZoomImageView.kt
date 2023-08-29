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
import androidx.core.view.ViewCompat
import coil.drawable.CrossfadeDrawable
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.SuccessResult
import coil.util.CoilUtils
import com.github.panpf.zoomimage.coil.internal.CoilImageSource
import com.github.panpf.zoomimage.coil.internal.CoilTileMemoryCache
import com.github.panpf.zoomimage.subsampling.ImageSource

/**
 * An ImageView that integrates the Coil image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val coilZoomImageView = CoilZoomImageView(context)
 * coilZoomImageView.load("http://sample.com/sample.jpg") {
 *     placeholder(R.drawable.placeholder)
 *     crossfade(true)
 * }
 * ```
 */
open class CoilZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    init {
        _subsamplingAbility?.tileMemoryCache = CoilTileMemoryCache(context.imageLoader)
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
            if (!isAttachedToWindow) {
                return@post
            }
            val result = CoilUtils.result(this)
            if (result == null) {
                logger.d { "CoilZoomImageView. Can't use Subsampling, result is null" }
                return@post
            }
            if (result !is SuccessResult) {
                logger.d { "CoilZoomImageView. Can't use Subsampling, result is not Success" }
                return@post
            }
            _subsamplingAbility?.disableMemoryCache = isDisallowMemoryCache(result)
            _subsamplingAbility?.setImageSource(newImageSource(result))
        }
    }

    private fun isDisallowMemoryCache(result: SuccessResult): Boolean {
        return result.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private fun newImageSource(result: SuccessResult): ImageSource? {
        val lastChildDrawable = result.drawable.getLastChildDrawable()
        if (lastChildDrawable !is BitmapDrawable) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, drawable is not BitmapDrawable" }
            return null
        }
        if (lastChildDrawable is Animatable) {
            logger.d { "CoilZoomImageView. Can't use Subsampling, drawable is Animatable" }
            return null
        }
        return CoilImageSource(context.imageLoader, result.request)
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
}