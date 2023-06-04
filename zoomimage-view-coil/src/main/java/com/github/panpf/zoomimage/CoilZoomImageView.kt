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
import android.content.ContextWrapper
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import coil.drawable.CrossfadeDrawable
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.SuccessResult
import coil.util.CoilUtils

open class CoilZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

//    private val listener =
//        object : Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error> {
//            override fun onStart(request: DisplayRequest) {
//                super.onStart(request)
//                val lifecycle = request.lifecycle
//                    .takeIf { !it.isSketchGlobalLifecycle() }
//                    ?: context.getLifecycle()
//                subsamplingAbility.setLifecycle(lifecycle)
//            }
//        }

    // todo memory cache
    init {
//        _subsamplingAbility?.tinyBitmapPool = SketchTinyBitmapPool(context.sketch)
//        _subsamplingAbility?.tinyMemoryCache = SketchTinyMemoryCache(context.sketch)
    }

//    override fun getDisplayListener(): Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>? {
//        return listener
//    }
//
//    override fun getDisplayProgressListener(): ProgressListener<DisplayRequest>? {
//        return null
//    }

    internal fun Context?.getLifecycle(): Lifecycle? {
        var context: Context? = this
        while (true) {
            when (context) {
                is LifecycleOwner -> return context.lifecycle
                is ContextWrapper -> context = context.baseContext
                else -> return null
            }
        }
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        post {
            // todo filter placeholder
            val result = CoilUtils.result(this)
            if (result != null && result is SuccessResult) {
                _subsamplingAbility?.disallowMemoryCache = getDisallowMemoryCache(result)
                _subsamplingAbility?.setImageSource(newImageSource(result))
                _subsamplingAbility?.setLifecycle(result.request.lifecycle)
            }
        }
    }

    private fun getDisallowMemoryCache(result: SuccessResult): Boolean {
        return result.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private fun newImageSource(result: SuccessResult): ImageSource? {
        val lastChildDrawable = result.drawable.getLastChildDrawable()
        if (lastChildDrawable !is BitmapDrawable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is not BitmapDrawable" }
            return null
        }
        if (lastChildDrawable is Animatable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is Animatable" }
            return null
        }
        return CoilImageSource(context.imageLoader, result.request)
    }

    companion object {
        const val MODULE = "CoilZoomImageView"
    }

    /**
     * Find the last child [Drawable] from the specified Drawable
     */
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