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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import com.github.panpf.zoomimage.picasso.PicassoDataToImageSource
import com.github.panpf.zoomimage.picasso.PicassoDataToImageSourceImpl
import com.github.panpf.zoomimage.picasso.PicassoTileBitmapCache
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.checkMemoryCacheDisabled
import com.squareup.picasso.internalMemoryPolicy
import java.io.File

/**
 * An ImageView that integrates the Picasso image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val picassoZoomImageView = PicassoZoomImageView(context)
 * picassoZoomImageViewImage.loadImage("http://sample.com/huge_world.jpeg") {
 *     placeholder(R.drawable.placeholder)
 * }
 * ```
 *
 * Note: PicassoZoomImageView provides a set of dedicated APIs to listen to load results and get URIs, so please do not use the official API directly to load images
 */
open class PicassoZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    private val convertors =
        mutableListOf<PicassoDataToImageSource>(PicassoDataToImageSourceImpl(context))

    init {
        _subsamplingEngine?.tileBitmapCacheState?.value = PicassoTileBitmapCache(Picasso.get())
    }

    /**
     * Start an image request using the specified image file. This is a convenience method for
     * calling load(Uri).
     *
     * Passing null as a file will not trigger any request but will set a
     * placeholder, if one is specified.
     *
     * Equivalent to calling load(Uri) load(Uri.fromFile(file)).
     *
     * @param file Image file
     * @param callback Callback for loading results
     * @param config Picasso Request configuration
     *
     * Note: Please use this API to load the image, only then can PicassoZoomImageView get the URI of the image and set the ImageSource to support the subsampling function after the listener load is successful
     */
    fun loadImage(
        file: File,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(file)
        config?.invoke(creator)
        loadImage(data = Uri.fromFile(file), callback, creator)
    }

    /**
     * Start an image request using the specified drawable resource ID.
     *
     * @param resourceId Image resource ID
     * @param callback Callback for loading results
     * @param config Picasso Request configuration
     *
     * Note: Please use this API to load the image, only then can PicassoZoomImageView get the URI of the image and set the ImageSource to support the subsampling function after the listener load is successful
     */
    fun loadImage(
        resourceId: Int,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(resourceId)
        config?.invoke(creator)
        loadImage(data = resourceId, callback, creator)
    }

    /**
     * Start an image request using the specified URI.
     *
     * Passing null as a uri will not trigger any request but will set a placeholder,
     * if one is specified.
     *
     * @param uri Image URI
     * @param callback Callback for loading results
     * @param config Picasso Request configuration
     *
     * Note: Please use this API to load the image, only then can PicassoZoomImageView get the URI of the image and set the ImageSource to support the subsampling function after the listener load is successful
     */
    fun loadImage(
        uri: Uri?,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(uri)
        config?.invoke(creator)
        loadImage(data = uri, callback, creator)
    }

    /**
     * Start an image request using the specified path. This is a convenience method for calling load(Uri).
     *
     * This path may be a remote URL, file resource (prefixed with 'file:'), content resource
     * (prefixed with 'content:'), or android resource (prefixed with 'android.resource:').
     *
     * Passing null as a path will not trigger any request but will set a placeholder, if one is specified.
     *
     * @param path Image path
     * @param callback Callback for loading results
     * @param config Picasso Request configuration
     *
     * Note: Please use this API to load the image, only then can PicassoZoomImageView get the URI of the image and set the ImageSource to support the subsampling function after the listener load is successful
     */
    fun loadImage(
        path: String?,
        callback: Callback? = null,
        config: (RequestCreator.() -> Unit)? = null
    ) {
        val creator = Picasso.get().load(path)
        config?.invoke(creator)
        loadImage(data = path?.let { Uri.parse(it) }, callback, creator)
    }

    private fun loadImage(
        data: Any?,
        callback: Callback?,
        creator: RequestCreator
    ) {
        creator.into(this, object : Callback {
            override fun onSuccess() {
                _subsamplingEngine?.disabledTileBitmapCacheState?.value =
                    checkMemoryCacheDisabled(creator.internalMemoryPolicy)
                val imageSource = if (data != null)
                    convertors.firstNotNullOfOrNull { it.dataToImageSource(data) } else null
                if (imageSource == null) {
                    logger.w { "PicassoZoomImageView. Can't use Subsampling, unsupported data: '$data'" }
                }
                _subsamplingEngine?.setImageSource(imageSource)
                callback?.onSuccess()
            }

            override fun onError(e: Exception?) {
                _subsamplingEngine?.setImageSource(null)
                callback?.onError(e)
            }
        })
    }

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        _subsamplingEngine?.disabledTileBitmapCacheState?.value = false
    }

    fun registerDataToImageSource(convertor: PicassoDataToImageSource) {
        convertors.add(0, convertor)
    }

    fun unregisterDataToImageSource(convertor: PicassoDataToImageSource) {
        convertors.remove(convertor)
    }
}