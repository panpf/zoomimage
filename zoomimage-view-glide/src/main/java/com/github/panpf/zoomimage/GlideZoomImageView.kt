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
import android.net.Uri
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.getModel
import com.bumptech.glide.load.engine.requestOptionsCompat
import com.bumptech.glide.request.SingleRequest
import com.github.panpf.zoomimage.core.ImageSource
import com.github.panpf.zoomimage.internal.GlideHttpImageSource
import com.github.panpf.zoomimage.internal.GlideTinyBitmapPool
import com.github.panpf.zoomimage.internal.GlideTinyMemoryCache
import java.io.File

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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (drawable != null) {
            resetImageSource()
        }
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        _subsamplingAbility?.disableMemoryCache = false
        if (ViewCompat.isAttachedToWindow(this)) {
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        post {
            if (!ViewCompat.isAttachedToWindow(this)) {
                return@post
            }
            val request = getTag(com.bumptech.glide.R.id.glide_custom_view_target_tag)
            if (request == null) {
                _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, request is null" }
                return@post
            }
            if (request !is SingleRequest<*>) {
                _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, request is not SingleRequest" }
                return@post
            }
            if (!request.isComplete) {
                _zoomAbility?.logger?.d(MODULE) { "Can't use Subsampling, request is not complete" }
                return@post
            }
            _subsamplingAbility?.disableMemoryCache = isDisableMemoryCache(request)
            _subsamplingAbility?.setImageSource(newImageSource(request))
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun isDisableMemoryCache(request: SingleRequest<*>): Boolean {
        val requestOptions = request.requestOptionsCompat
        return !requestOptions.isMemoryCacheable()
    }

    private fun newImageSource(request: SingleRequest<*>): ImageSource? {
        val model = request.getModel()
        return when {
            model is String && (model.startsWith("http://") || model.startsWith("https://")) -> {
                GlideHttpImageSource(Glide.get(context), model)
            }

            model is String && model.startsWith("content://") -> {
                ImageSource.fromContent(context, Uri.parse(model))
            }

            model is String && model.startsWith("file:///android_asset/") -> {
                val assetFileName = model.replace("file:///android_asset/", "")
                ImageSource.fromAsset(context, assetFileName)
            }

            model is String && model.startsWith("file://") -> {
                ImageSource.fromFile(File(model.replace("file://", "")))
            }

            model is Int -> {
                ImageSource.fromResource(context, model)
            }

            else -> {
                _zoomAbility?.logger?.w(MODULE) { "Can't use Subsampling, unsupported model: '$model'" }
                null
            }
        }
    }
}