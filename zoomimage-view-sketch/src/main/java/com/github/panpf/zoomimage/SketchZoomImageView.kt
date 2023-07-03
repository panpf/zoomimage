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
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.displayResult
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.isSketchGlobalLifecycle
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.internal.SketchStateDrawable
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.sketch.util.findLastSketchDrawable
import com.github.panpf.sketch.util.getLastChildDrawable
import com.github.panpf.zoomimage.internal.SketchImageSource
import com.github.panpf.zoomimage.internal.SketchTileBitmapPool
import com.github.panpf.zoomimage.internal.SketchTileMemoryCache
import com.github.panpf.zoomimage.internal.getLifecycle
import com.github.panpf.zoomimage.core.imagesource.ImageSource

open class SketchZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle), ImageOptionsProvider {

    companion object {
        const val MODULE = "SketchZoomImageView"
    }

    override var displayImageOptions: ImageOptions? = null

    init {
        _subsamplingAbility?.tileBitmapPool = SketchTileBitmapPool(context.sketch)
        _subsamplingAbility?.tileMemoryCache = SketchTileMemoryCache(context.sketch)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (drawable != null) {
            resetImageSource()
        }
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        _subsamplingAbility?.disableMemoryCache = false
        _subsamplingAbility?.disallowReuseBitmap = false
        _subsamplingAbility?.ignoreExifOrientation = false
        _subsamplingAbility?.setLifecycle(context.getLifecycle())
        if (ViewCompat.isAttachedToWindow(this)) {
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        post {
            if (!ViewCompat.isAttachedToWindow(this)) {
                return@post
            }
            val result = displayResult
            if (result == null) {
                _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, result is null" }
                return@post
            }
            if (result !is DisplayResult.Success) {
                _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, result is not Success" }
                return@post
            }
            _subsamplingAbility?.disableMemoryCache = isDisableMemoryCache(result.drawable)
            _subsamplingAbility?.disallowReuseBitmap = isDisallowReuseBitmap(result.drawable)
            _subsamplingAbility?.ignoreExifOrientation = isIgnoreExifOrientation(result.drawable)
            _subsamplingAbility?.setLifecycle(result.request.lifecycle
                .takeIf { !it.isSketchGlobalLifecycle() }
                ?: context.getLifecycle())
            _subsamplingAbility?.setImageSource(newImageSource(result.drawable))
        }
    }

    private fun isDisableMemoryCache(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private fun isDisallowReuseBitmap(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.disallowReuseBitmap
    }

    private fun isIgnoreExifOrientation(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.ignoreExifOrientation
    }

    private fun newImageSource(drawable: Drawable?): ImageSource? {
        drawable ?: return null
        if (drawable.getLastChildDrawable() is SketchStateDrawable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, drawable is SketchStateDrawable" }
            return null
        }
        val sketchDrawable = drawable.findLastSketchDrawable()
        if (sketchDrawable == null) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, drawable is not SketchDrawable" }
            return null
        }
        if (sketchDrawable is Animatable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, drawable is Animatable" }
            return null
        }
        return SketchImageSource(
            context = context,
            sketch = context.sketch,
            imageUri = sketchDrawable.imageUri,
        )
    }
}