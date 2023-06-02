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
package com.github.panpf.zoom.sample.ui.widget

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.WorkerThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.datasource.BasedStreamDataSource
import com.github.panpf.sketch.decode.internal.ImageFormat
import com.github.panpf.sketch.decode.internal.supportBitmapRegionDecoder
import com.github.panpf.sketch.request.DisplayListenerProvider
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.ImageOptions
import com.github.panpf.sketch.request.ImageOptionsProvider
import com.github.panpf.sketch.request.Listener
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.ProgressListener
import com.github.panpf.sketch.request.isSketchGlobalLifecycle
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.internal.SketchStateDrawable
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.sketch.util.findLastSketchDrawable
import com.github.panpf.sketch.util.getLastChildDrawable
import com.github.panpf.zoom.ImageSource
import com.github.panpf.zoom.SubsamplingImageView
import com.github.panpf.zoom.internal.canUseSubsampling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

open class SketchSubsamplingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : SubsamplingImageView(context, attrs, defStyle), ImageOptionsProvider, DisplayListenerProvider {

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
//        _zoomAbility?.setImageSize(readImageSize(newDrawable))
        _subsamplingAbility?.setImageSource(newImageSource(newDrawable))
        resetConfig(newDrawable)
    }

//    private fun readImageSize(drawable: Drawable?): Size {
//        val sketchDrawable = drawable?.findLastSketchDrawable()
//        return sketchDrawable
//            ?.let { Size(it.imageInfo.width, it.imageInfo.height) }
//            ?: Size.EMPTY
//    }

    private fun newImageSource(drawable: Drawable?): ImageSource? {
        drawable ?: return null
        if (drawable.getLastChildDrawable() is SketchStateDrawable) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is SketchStateDrawable" }
            return null
        }
        val sketchDrawable = drawable.findLastSketchDrawable()
        if (sketchDrawable == null) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is not SketchDrawable" }
            return null
        }
        if (sketchDrawable is Animatable) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is Animatable" }
            return null
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val imageWidth = sketchDrawable.imageInfo.width
        val imageHeight = sketchDrawable.imageInfo.height
        val mimeType = sketchDrawable.imageInfo.mimeType
        val requestKey = sketchDrawable.requestKey

        if (drawableWidth >= imageWidth && drawableHeight >= imageHeight) {
            logger.d(MODULE) {
                "Can't use Subsampling. The Drawable size is greater than or equal to the original image. drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: S$mimeType. '$requestKey'"
            }
            return null
        }
        if (!canUseSubsampling(imageWidth, imageHeight, drawableWidth, drawableHeight)) {
            logger.d(MODULE) {
                "Can't use Subsampling. The drawable aspect ratio is inconsistent with the original image. drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: S$mimeType. '$requestKey'"
            }
            return null
        }
        if (ImageFormat.parseMimeType(mimeType)?.supportBitmapRegionDecoder() != true) {
            logger.d(MODULE) {
                "Can't use Subsampling. Image type not support subsampling. drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: S$mimeType. '$requestKey'"
            }
            return null
        }

        logger.d(MODULE) {
            "Use Subsampling. drawableSize: ${drawableWidth}x${drawableHeight}, imageSize: ${imageWidth}x${imageHeight}, mimeType: S$mimeType. '$requestKey'"
        }
        return SketchImageSource(
            context = context,
            sketch = context.sketch,
            imageUri = sketchDrawable.imageUri,
        )
    }

    private fun resetConfig(drawable: Drawable?) {
        var disallowMemoryCache = false
        var disallowReuseBitmap = false
        val sketchDrawable = drawable?.findLastSketchDrawable()
        val requestKey = sketchDrawable?.requestKey
        val displayResult = SketchUtils.getResult(this)
        if (displayResult != null
            && displayResult is DisplayResult.Success
            && displayResult.requestKey == requestKey
        ) {
            disallowMemoryCache = displayResult.request.memoryCachePolicy != CachePolicy.ENABLED
            disallowReuseBitmap = displayResult.request.disallowReuseBitmap
        }
        _subsamplingAbility?.disallowMemoryCache = disallowMemoryCache
        _subsamplingAbility?.disallowReuseBitmap = disallowReuseBitmap
    }

    companion object {
        const val MODULE = "SketchSubsamplingImageView"
    }

    class SketchImageSource(
        private val context: Context,
        private val sketch: Sketch,
        private val imageUri: String,
    ) : ImageSource {

        override val key: String = imageUri

        @WorkerThread
        override suspend fun openInputStream(): Result<InputStream> {
            val request = LoadRequest(context, imageUri) {
                downloadCachePolicy(CachePolicy.ENABLED)
            }
            val fetcher = try {
                sketch.components.newFetcherOrThrow(request)
            } catch (e: Exception) {
                return Result.failure(e)
            }
            val fetchResult = withContext(Dispatchers.IO) {
                fetcher.fetch()
            }.let {
                it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!)
            }
            val dataSource = fetchResult.dataSource
            return if (dataSource is BasedStreamDataSource) {
                try {
                    dataSource.newInputStream().let { Result.success(it) }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(IllegalStateException("DataSource is not BasedStreamDataSource"))
            }
        }
    }
}