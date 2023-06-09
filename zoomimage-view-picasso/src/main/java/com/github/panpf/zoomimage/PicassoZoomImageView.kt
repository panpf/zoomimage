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
import com.github.panpf.zoomimage.imagesource.ImageSource
import com.github.panpf.zoomimage.view.picasso.internal.PicassoHttpImageSource
import com.github.panpf.zoomimage.view.picasso.internal.PicassoTileMemoryCache
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.isDisableMemoryCache
import com.squareup.picasso.memoryPolicy
import java.io.File

open class PicassoZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    companion object {
        const val MODULE = "PicassoZoomImageView"
    }

    init {
        _subsamplingAbility?.tileMemoryCache = PicassoTileMemoryCache(Picasso.get())
    }

    fun loadImage(
        file: File,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(file)
        config?.invoke(creator)
        loadImage(Uri.fromFile(file), callback, creator)
    }

    fun loadImage(
        resourceId: Int,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(resourceId)
        config?.invoke(creator)
        loadImage(Uri.parse("android.resource://$resourceId"), callback, creator)
    }

    fun loadImage(
        uri: Uri?,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(uri)
        config?.invoke(creator)
        loadImage(uri, callback, creator)
    }

    fun loadImage(
        path: String?,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(path)
        config?.invoke(creator)
        loadImage(path?.let { Uri.parse(it) }, callback, creator)
    }

    private fun loadImage(uri: Uri?, callback: Callback?, creator: RequestCreator) {
        creator.into(this, object : Callback {
            override fun onSuccess() {
                _subsamplingAbility?.disableMemoryCache =
                    isDisableMemoryCache(creator.memoryPolicy)
                _subsamplingAbility?.setImageSource(newImageSource(uri))
                callback?.onSuccess()
            }

            override fun onError(e: Exception?) {
                callback?.onError(e)
            }
        })
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        _subsamplingAbility?.disableMemoryCache = false
    }

    private fun newImageSource(uri: Uri?): ImageSource? {
        uri ?: return null
        val uriString: String = uri.toString()
        return when {
            uriString.startsWith("http://") || uriString.startsWith("https://") -> {
                PicassoHttpImageSource(Picasso.get(), uri)
            }

            uriString.startsWith("content://") -> {
                ImageSource.fromContent(context, uri)
            }

            uriString.startsWith("file:///android_asset/") -> {
                val assetFileName = uriString.replace("file:///android_asset/", "")
                ImageSource.fromAsset(context, assetFileName)
            }

            uriString.startsWith("file://") -> {
                ImageSource.fromFile(File(uriString.replace("file://", "")))
            }

            uriString.startsWith("android.resource://") -> {
                val resId = uriString.replace("android.resource://", "").toIntOrNull()
                resId?.let { ImageSource.fromResource(context, it) }
            }

            else -> {
                _zoomAbility?.logger?.w(MODULE) { "Can't use Subsampling, unsupported uri: '$uri'" }
                null
            }
        }
    }
}