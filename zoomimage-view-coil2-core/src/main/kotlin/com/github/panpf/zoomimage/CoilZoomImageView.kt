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
import coil.request.SuccessResult
import coil.util.CoilUtils
import com.github.panpf.zoomimage.coil.CoilTileImageCache
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.view.coil.CoilViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.coil.internal.EngineCoilViewSubsamplingImageGenerator
import com.github.panpf.zoomimage.view.coil.internal.getImageLoader
import kotlinx.coroutines.launch

/**
 * An ImageView that integrates the Coil image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val coilZoomImageView = CoilZoomImageView(context)
 * coilZoomImageView.load("https://sample.com/sample.jpeg") {
 *     placeholder(R.drawable.placeholder)
 *     crossfade(true)
 * }
 * ```
 *
 * @see com.github.panpf.zoomimage.view.coil2.core.test.CoilZoomImageViewTest
 */
open class CoilZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ZoomImageView(context, attrs, defStyle) {

    private val subsamplingImageGenerators = mutableListOf<CoilViewSubsamplingImageGenerator>()
    private var resetImageSourceOnAttachedToWindow: Boolean = false

    fun registerSubsamplingImageGenerator(convertor: CoilViewSubsamplingImageGenerator) {
        subsamplingImageGenerators.add(0, convertor)
    }

    fun unregisterSubsamplingImageGenerator(convertor: CoilViewSubsamplingImageGenerator) {
        subsamplingImageGenerators.remove(convertor)
    }

    override fun newLogger(): Logger = Logger(tag = "CoilZoomImageView")

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
        // You must use post to delay execution because 'CoilUtils.result' may not be ready when onDrawableChanged is executed.
        post {
            if (!isAttachedToWindow) {
                resetImageSourceOnAttachedToWindow = true
                return@post
            }

            val subsamplingEngine = _subsamplingEngine ?: return@post
            val tileImageCacheState = subsamplingEngine.tileImageCacheState
            val imageLoader = CoilUtils.getImageLoader(this@CoilZoomImageView)
            if (tileImageCacheState.value == null && imageLoader != null) {
                tileImageCacheState.value = CoilTileImageCache(imageLoader)
            }

            val result = CoilUtils.result(this)
            val drawable = drawable
            if (imageLoader != null && result is SuccessResult && drawable != null) {
                val coroutineScope = coroutineScope!!
                coroutineScope.launch {
                    val model = result.request.data
                    val generateResult = subsamplingImageGenerators.plus(
                        EngineCoilViewSubsamplingImageGenerator()
                    ).firstNotNullOfOrNull {
                        it.generateImage(context, imageLoader, result.request, result, drawable)
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