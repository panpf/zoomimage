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
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.io.File

open class PicassoZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    companion object {
        const val MODULE = "PicassoZoomImageView"
    }

    // todo 适配

    init {
//        _subsamplingAbility?.tinyBitmapPool = PicassoTinyBitmapPool(context.sketch)
//        _subsamplingAbility?.tinyMemoryCache = PicassoTinyMemoryCache(context.sketch)
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
                val uriString = uri.toString()
                // todo 适配多种 url
                if (uriString.startsWith("file:///android_asset/")) {
                    val assetFileName = uriString.replace("file:///android_asset/", "")
                    val imageSource = ImageSource.fromAsset(context, assetFileName)
                    _subsamplingAbility?.setImageSource(imageSource)
                } else {
                    _subsamplingAbility?.setImageSource(null)
                }
                callback?.onSuccess()
            }

            override fun onError(e: Exception?) {
                callback?.onError(e)
            }
        })
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        _subsamplingAbility?.setImageSource(null)
    }

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
//        return PicassoImageSource(
//            context = context,
//            sketch = context.sketch,
//            imageUri = sketchDrawable.imageUri,
//        )
//    }
}