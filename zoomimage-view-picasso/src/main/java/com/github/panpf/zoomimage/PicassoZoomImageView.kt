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
import com.github.panpf.zoomimage.picasso.PicassoHttpImageSource
import com.github.panpf.zoomimage.picasso.PicassoTileBitmapCache
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.fromContent
import com.github.panpf.zoomimage.subsampling.fromResource
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
 * picassoZoomImageViewImage.loadImage("http://sample.com/sample.jpg") {
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
        loadImage(Uri.fromFile(file), callback, creator)
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
        loadImage(Uri.parse("android.resource://$resourceId"), callback, creator)
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
        loadImage(uri, callback, creator)
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
        loadImage(path?.let { Uri.parse(it) }, callback, creator)
    }

    private fun loadImage(uri: Uri?, callback: Callback?, creator: RequestCreator) {
        creator.into(this, object : Callback {
            override fun onSuccess() {
                _subsamplingEngine?.disableTileBitmapCacheState?.value =
                    checkMemoryCacheDisabled(creator.internalMemoryPolicy)
                _subsamplingEngine?.setImageSource(newImageSource(uri))
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
        _subsamplingEngine?.disableTileBitmapCacheState?.value = false
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
                logger.w { "PicassoZoomImageView. Can't use Subsampling, unsupported uri: '$uri'" }
                null
            }
        }
    }
}