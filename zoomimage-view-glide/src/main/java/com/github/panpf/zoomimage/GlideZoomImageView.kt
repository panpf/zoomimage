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
import com.bumptech.glide.load.engine.getUrl
import com.bumptech.glide.load.engine.requestOptionsCompat
import com.bumptech.glide.request.SingleRequest
import com.github.panpf.zoomimage.internal.GlideTinyBitmapPool
import com.github.panpf.zoomimage.internal.GlideTinyMemoryCache

open class GlideZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    companion object {
        const val MODULE = "GlideZoomImageView"
    }

    init {
        val glide = Glide.get(context)
        _subsamplingAbility?.tinyBitmapPool = GlideTinyBitmapPool(glide)
        _subsamplingAbility?.tinyMemoryCache = GlideTinyMemoryCache(glide)
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        post {
            if (!ViewCompat.isAttachedToWindow(this)) return@post
            val request = getTag(com.bumptech.glide.R.id.glide_custom_view_target_tag)
            if (request != null && request is SingleRequest<*> && request.isComplete) {
                _subsamplingAbility?.disallowMemoryCache = isDisallowMemoryCache(request)
                _subsamplingAbility?.setImageSource(newImageSource(request))
            } else {
                _subsamplingAbility?.disallowMemoryCache = false
                _subsamplingAbility?.setImageSource(null)
            }
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun isDisallowMemoryCache(request: SingleRequest<*>): Boolean {
        val requestOptions = request.requestOptionsCompat
        return !requestOptions.isMemoryCacheable() || requestOptions.isSkipMemoryCacheSet()
    }

    private fun newImageSource(request: SingleRequest<*>): ImageSource? {
        val url = request.getUrl() ?: return null
        // todo 适配多种 url
        return when {
            url.startsWith("file:///android_asset/") -> {
                val assetFileName = url.replace("file:///android_asset/", "")
                ImageSource.fromAsset(context, assetFileName)
            }

            else -> {
                null
            }
        }
    }
}