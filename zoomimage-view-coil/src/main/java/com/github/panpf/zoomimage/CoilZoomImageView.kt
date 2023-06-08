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
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.SuccessResult
import coil.util.CoilUtils
import com.github.panpf.zoomimage.internal.CoilImageSource
import com.github.panpf.zoomimage.internal.CoilTinyMemoryCache
import com.github.panpf.zoomimage.internal.getLastChildDrawable
import com.github.panpf.zoomimage.internal.getLifecycle
import com.github.panpf.zoomimage.internal.isCoilGlobalLifecycle

open class CoilZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    companion object {
        const val MODULE = "CoilZoomImageView"
    }

    init {
        _subsamplingAbility?.tinyMemoryCache = CoilTinyMemoryCache(context.imageLoader)
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        post {
            if (!isAttachedToWindow) return@post
            val result = CoilUtils.result(this)
            if (result != null && result is SuccessResult) {
                _subsamplingAbility?.disallowMemoryCache = isDisallowMemoryCache(result)
                _subsamplingAbility?.setLifecycle(result.request.lifecycle
                    .takeIf { !it.isCoilGlobalLifecycle() }
                    ?: context.getLifecycle())
                _subsamplingAbility?.setImageSource(newImageSource(result))
            } else {
                _subsamplingAbility?.disallowMemoryCache = false
                _subsamplingAbility?.setImageSource(null)
                _subsamplingAbility?.setLifecycle(context.getLifecycle())
            }
        }
    }

    private fun isDisallowMemoryCache(result: SuccessResult): Boolean {
        return result.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private fun newImageSource(result: SuccessResult): ImageSource? {
        val lastChildDrawable = result.drawable.getLastChildDrawable()
        if (lastChildDrawable !is BitmapDrawable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, drawable is not BitmapDrawable" }
            return null
        }
        if (lastChildDrawable is Animatable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, drawable is Animatable" }
            return null
        }
        return CoilImageSource(context.imageLoader, result.request)
    }
}