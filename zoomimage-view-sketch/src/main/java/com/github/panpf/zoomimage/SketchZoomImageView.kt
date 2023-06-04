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
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.decode.internal.ImageFormat
import com.github.panpf.sketch.decode.internal.supportBitmapRegionDecoder
import com.github.panpf.sketch.request.DisplayListenerProvider
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.isSketchGlobalLifecycle
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.internal.SketchStateDrawable
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.sketch.util.findLastSketchDrawable
import com.github.panpf.sketch.util.getLastChildDrawable
import com.github.panpf.zoomimage.internal.canUseSubsampling

open class SketchZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle), ImageOptionsProvider, DisplayListenerProvider {

    companion object {
        const val MODULE = "SketchZoomImageView"
    }

    override var displayImageOptions: ImageOptions? = null

    private val listener =
        object : Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error> {
            override fun onStart(request: DisplayRequest) {
                super.onStart(request)
                val lifecycle = request.lifecycle
                    .takeIf { !it.isSketchGlobalLifecycle() }
                    ?: context.getLifecycle()
                subsamplingAbility.setLifecycle(lifecycle)
            }
        }

    init {
        _subsamplingAbility?.tinyBitmapPool = SketchTinyBitmapPool(context.sketch)
        _subsamplingAbility?.tinyMemoryCache = SketchTinyMemoryCache(context.sketch)
    }

    override fun getDisplayListener(): Listener<DisplayRequest, DisplayResult.Success, DisplayResult.Error>? {
        return listener
    }

    override fun getDisplayProgressListener(): ProgressListener<DisplayRequest>? {
        return null
    }

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
        _subsamplingAbility?.disallowMemoryCache = getDisallowMemoryCache(newDrawable)
        _subsamplingAbility?.disallowReuseBitmap = getDisallowReuseBitmap(newDrawable)
        _subsamplingAbility?.setImageSource(newImageSource(newDrawable))
    }

    private fun getDisallowMemoryCache(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.memoryCachePolicy != CachePolicy.ENABLED
    }

    private fun getDisallowReuseBitmap(drawable: Drawable?): Boolean {
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        return displayResult != null
                && displayResult is DisplayResult.Success
                && displayResult.requestKey == requestKey
                && displayResult.request.disallowReuseBitmap
    }

    private fun newImageSource(drawable: Drawable?): ImageSource? {
        drawable ?: return null
        if (drawable.getLastChildDrawable() is SketchStateDrawable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is SketchStateDrawable" }
            return null
        }
        val sketchDrawable = drawable.findLastSketchDrawable()
        if (sketchDrawable == null) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is not SketchDrawable" }
            return null
        }
        if (sketchDrawable is Animatable) {
            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is Animatable" }
            return null
        }
        return SketchImageSource(
            context = context,
            sketch = context.sketch,
            imageUri = sketchDrawable.imageUri,
        )
    }
}