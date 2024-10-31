/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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
import com.bumptech.glide.Glide
import com.bumptech.glide.getRequestFromView
import com.bumptech.glide.internalModel
import com.github.panpf.zoomimage.glide.GlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.glide.GlideTileImageCache
import com.github.panpf.zoomimage.glide.internal.EngineGlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.launch

/**
 * An ImageView that integrates the Glide image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val glideZoomImageView = GlideZoomImageView(context)
 * Glide.with(this@GlideZoomImageViewFragment)
 *     .load("https://sample.com/sample.jpeg")
 *     .placeholder(R.drawable.placeholder)
 *     .into(glideZoomImageView)
 * ```
 *
 * @see com.github.panpf.zoomimage.view.glide.test.GlideZoomImageViewTest
 */
open class GlideZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    private val subsamplingImageGenerators = mutableListOf<GlideSubsamplingImageGenerator>()
    private var resetImageSourceOnAttachedToWindow: Boolean = false

    fun registerSubsamplingImageGenerator(convertor: GlideSubsamplingImageGenerator) {
        subsamplingImageGenerators.add(0, convertor)
    }

    fun unregisterSubsamplingImageGenerator(convertor: GlideSubsamplingImageGenerator) {
        subsamplingImageGenerators.remove(convertor)
    }

    override fun newLogger(): Logger = Logger(tag = "GlideZoomImageView")

    override fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        super.onDrawableChanged(oldDrawable, newDrawable)
        if (isAttachedToWindow) {
            resetImageSource()
        } else {
            resetImageSourceOnAttachedToWindow = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (resetImageSourceOnAttachedToWindow) {
            resetImageSourceOnAttachedToWindow = false
            resetImageSource()
        }
    }

    private fun resetImageSource() {
        // You must use post to delay execution because 'request.isComplete' may not be ready when onDrawableChanged is executed.
        post {
            if (!isAttachedToWindow) {
                resetImageSourceOnAttachedToWindow = true
                return@post
            }

            // In order to be consistent with other ZoomImageViews, TileImageCache is also configured here,
            // although it can be set in the constructor
            val subsamplingEngine = _subsamplingEngine ?: return@post
            val tileImageCacheState = subsamplingEngine.tileImageCacheState
            if (tileImageCacheState.value == null) {
                tileImageCacheState.value = GlideTileImageCache(Glide.get(context))
            }

            val coroutineScope = coroutineScope!!
            val request = getRequestFromView(this@GlideZoomImageView)
            val model: Any? = request?.internalModel
            if (request != null && request.isComplete && model != null) {
                coroutineScope.launch {
                    // TODO filter animatable painter
                    val generateResult = subsamplingImageGenerators.plus(
                        EngineGlideSubsamplingImageGenerator()
                    ).firstNotNullOfOrNull {
                        it.generateImage(context, Glide.get(context), model, drawable)
                    }
                    if (generateResult is SubsamplingImageGenerateResult.Error) {
                        logger.d {
                            "GlideZoomImageView. ${generateResult.message}. model='$model'"
                        }
                    }
                    if (generateResult is SubsamplingImageGenerateResult.Success) {
                        setImage(generateResult.subsamplingImage)
                    } else {
                        setImage(null as SubsamplingImage?)
                    }
                }
            } else {
                setImage(null as SubsamplingImage?)
            }
        }
    }
}