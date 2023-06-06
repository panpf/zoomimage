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
import com.bumptech.glide.request.SingleRequest
import com.github.panpf.zoomimage.internal.getLifecycle

open class GlideZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    companion object {
        const val MODULE = "SketchZoomImageView"
    }

    // todo 适配

    init {
//        _subsamplingAbility?.tinyBitmapPool = GlideTinyBitmapPool(context.sketch)
//        _subsamplingAbility?.tinyMemoryCache = GlideTinyMemoryCache(context.sketch)
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        post {
            if (!ViewCompat.isAttachedToWindow(this)) return@post
            val request = getTag(com.bumptech.glide.R.id.glide_custom_view_target_tag)
            if (request != null && request is SingleRequest<*> && request.isComplete) {
                val field = request.javaClass.getDeclaredField("model")
                field.isAccessible = true
                val model = field.get(request)
                val url = model?.toString().orEmpty()
                // todo 适配个多种 url
                if (url.startsWith("file:///android_asset/")) {
                    val assetFileName = url.replace("file:///android_asset/", "")
                    val imageSource = ImageSource.fromAsset(context, assetFileName)
                    _subsamplingAbility?.setImageSource(imageSource)
                } else {
                    _subsamplingAbility?.setImageSource(null)
                }
//                _subsamplingAbility?.disallowMemoryCache = getDisallowMemoryCache(result.drawable)
//                _subsamplingAbility?.disallowReuseBitmap = getDisallowReuseBitmap(result.drawable)
//                _subsamplingAbility?.setLifecycle(result.request.lifecycle
//                    .takeIf { !it.isSketchGlobalLifecycle() }
//                    ?: context.getLifecycle())
//                _subsamplingAbility?.setImageSource(newImageSource(result.drawable))
            } else {
                _subsamplingAbility?.disallowMemoryCache = false
                _subsamplingAbility?.disallowReuseBitmap = false
                _subsamplingAbility?.setImageSource(null)
                _subsamplingAbility?.setLifecycle(context.getLifecycle())
            }
        }
    }
//
//    private fun getDisallowMemoryCache(drawable: Drawable?): Boolean {
//        val sketchDrawable = drawable?.findLastSketchDrawable()
//        val requestKey = sketchDrawable?.requestKey
//        val displayResult = SketchUtils.getResult(this)
//        return displayResult != null
//                && displayResult is DisplayResult.Success
//                && displayResult.requestKey == requestKey
//                && displayResult.request.memoryCachePolicy != CachePolicy.ENABLED
//    }
//
//    private fun getDisallowReuseBitmap(drawable: Drawable?): Boolean {
//        val sketchDrawable = drawable?.findLastSketchDrawable()
//        val requestKey = sketchDrawable?.requestKey
//        val displayResult = SketchUtils.getResult(this)
//        return displayResult != null
//                && displayResult is DisplayResult.Success
//                && displayResult.requestKey == requestKey
//                && displayResult.request.disallowReuseBitmap
//    }
//
//    private fun newImageSource(drawable: Drawable?): ImageSource? {
//        drawable ?: return null
//        if (drawable.getLastChildDrawable() is SketchStateDrawable) {
//            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is SketchStateDrawable" }
//            return null
//        }
//        val sketchDrawable = drawable.findLastSketchDrawable()
//        if (sketchDrawable == null) {
//            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is not SketchDrawable" }
//            return null
//        }
//        if (sketchDrawable is Animatable) {
//            _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling. Drawable is Animatable" }
//            return null
//        }
//        return GlideImageSource(
//            context = context,
//            sketch = context.sketch,
//            imageUri = sketchDrawable.imageUri,
//        )
//    }
}