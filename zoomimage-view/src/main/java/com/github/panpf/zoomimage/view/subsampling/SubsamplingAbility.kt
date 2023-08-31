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

package com.github.panpf.zoomimage.view.subsampling

import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.view.internal.findLifecycle
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.subsampling.internal.SubsamplingEngine
import com.github.panpf.zoomimage.view.subsampling.internal.TileDrawHelper

/**
 * Wrap [SubsamplingEngine] and connect [SubsamplingEngine] and [ImageView]
 */
class SubsamplingAbility(private val view: View, logger: Logger) {

    val logger: Logger = logger.newLogger(module = "SubsamplingAbility")
    internal val engine: SubsamplingEngine = SubsamplingEngine(this.logger)
    private var lifecycle: Lifecycle? = null
    private var imageSource: ImageSource? = null
    private val resetStoppedLifecycleObserver by lazy { ResetStoppedLifecycleObserver(this) }
    private val tileDrawHelper = TileDrawHelper(engine)

    init {
        view.post {
            val lifecycle1 =
                view.findViewTreeLifecycleOwner()?.lifecycle ?: view.context.findLifecycle()
            setLifecycle(lifecycle1)
        }
    }

    /* *********************************** Configurable properties ****************************** */

    /**
     * If true, the Exif rotation information for the image is ignored
     */
    var ignoreExifOrientation: Boolean
        get() = engine.ignoreExifOrientation
        set(value) {
            engine.ignoreExifOrientation = value
        }

    /**
     * Set up the tile memory cache container
     */
    var tileMemoryCache: TileMemoryCache?
        get() = engine.tileMemoryCache
        set(value) {
            engine.tileMemoryCache = value
        }

    /**
     * If true, disable memory cache
     */
    var disableMemoryCache: Boolean
        get() = engine.disableMemoryCache
        set(value) {
            engine.disableMemoryCache = value
        }

    /**
     * Set up a shared Bitmap pool for the tile
     */
    var tileBitmapPool: TileBitmapPool?
        get() = engine.tileBitmapPool
        set(value) {
            engine.tileBitmapPool = value
        }

    /**
     * If true, Bitmap reuse is disabled
     */
    var disallowReuseBitmap: Boolean
        get() = engine.disallowReuseBitmap
        set(value) {
            engine.disallowReuseBitmap = value
        }

    /**
     * Whether to pause loading tiles when transforming
     */
    var pauseWhenTransforming: Boolean
        get() = engine.pauseWhenTransforming
        set(value) {
            engine.pauseWhenTransforming = value
        }

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    var stopped: Boolean
        get() = engine.stopped
        set(value) {
            engine.stopped = value
        }

    /**
     * If true, the bounds of each tile is displayed
     */
    var showTileBounds = false
        set(value) {
            if (field != value) {
                field = value
                view.invalidate()
            }
        }

    /**
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec
        get() = engine.tileAnimationSpec
        set(value) {
            engine.tileAnimationSpec = value
        }


    /* *********************************** Information properties ******************************* */

    /**
     * The information of the image, including width, height, format, exif information, etc
     */
    val imageInfo: ImageInfo?
        get() = engine.imageInfo

    /**
     * Whether the image is ready for subsampling
     */
    val ready: Boolean
        get() = engine.ready

    /**
     * A snapshot of the tile list
     */
    val tileSnapshotList: List<TileSnapshot>
        get() = engine.tileSnapshotList

    /**
     * The image load rect
     */
    val imageLoadRect: IntRectCompat
        get() = engine.imageLoadRect

    init {
        engine.registerOnTileChangedListener {
            view.invalidate()
        }
    }


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set up an image source from which image tile are loaded
     */
    fun setImageSource(imageSource: ImageSource?) {
        if (this.imageSource == imageSource) return
        this.imageSource = imageSource
        if (view.isAttachedToWindowCompat) {
            engine.setImageSource(imageSource)
        }
    }

    /**
     * Set the lifecycle, which automatically controls stop and start, which is obtained from View.findViewTreeLifecycleOwner() by default,
     * and can be set by this method if the default acquisition method is not applicable
     */
    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetStopped("setLifecycle")
        }
    }

    /**
     * Register a [tileSnapshotList] property change listener
     */
    fun registerOnTileChangedListener(listener: OnTileChangeListener) =
        engine.registerOnTileChangedListener(listener)

    /**
     * Unregister a [tileSnapshotList] property change listener
     */
    fun unregisterOnTileChangedListener(listener: OnTileChangeListener): Boolean =
        engine.unregisterOnTileChangedListener(listener)

    /**
     * Register a [ready] property change listener
     */
    fun registerOnReadyChangeListener(listener: OnReadyChangeListener) =
        engine.registerOnReadyChangeListener(listener)

    /**
     * Unregister a [ready] property change listener
     */
    fun unregisterOnReadyChangeListener(listener: OnReadyChangeListener): Boolean =
        engine.unregisterOnReadyChangeListener(listener)

    /**
     * Register a [stopped] property change listener
     */
    fun registerOnStoppedChangeListener(listener: OnStoppedChangeListener) =
        engine.registerOnStoppedChangeListener(listener)

    /**
     * Unregister a [stopped] property change listener
     */
    fun unregisterOnStoppedChangeListener(listener: OnStoppedChangeListener): Boolean =
        engine.unregisterOnStoppedChangeListener(listener)

    /**
     * Register a [imageLoadRect] property change listener
     */
    fun registerOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener) =
        engine.registerOnImageLoadRectChangeListener(listener)

    /**
     * Unregister a [imageLoadRect] property change listener
     */
    fun unregisterOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener): Boolean =
        engine.unregisterOnImageLoadRectChangeListener(listener)


    /* *********************************** Interact with View *********************************** */

    internal fun onAttachedToWindow() {
        engine.setImageSource(imageSource)
        registerLifecycleObserver()
    }

    internal fun onDetachedFromWindow() {
        engine.setImageSource(null)
        unregisterLifecycleObserver()
    }

    internal fun onDraw(canvas: Canvas, transform: TransformCompat, containerSize: IntSizeCompat) {
        tileDrawHelper.drawTiles(canvas, transform, containerSize, showTileBounds)
    }

    internal fun onVisibilityChanged(visibility: Int) {
        val visibilityName = when (visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }
        resetStopped("onVisibilityChanged:$visibilityName")
    }


    /* *************************************** Internal ***************************************** */

    private fun resetStopped(caller: String) {
        val viewVisible = view.isVisible
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val stopped = !viewVisible || !lifecycleStarted
        logger.d {
            "resetStopped:$caller. $stopped. " +
                    "viewVisible=$viewVisible, " +
                    "lifecycleStarted=$lifecycleStarted. " +
                    "'${imageSource?.key}'"
        }
        engine.stopped = stopped
    }

    private fun registerLifecycleObserver() {
        if (view.isAttachedToWindowCompat) {
            lifecycle?.addObserver(resetStoppedLifecycleObserver)
        }
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(resetStoppedLifecycleObserver)
    }

    private class ResetStoppedLifecycleObserver(
        val subsamplingAbility: SubsamplingAbility
    ) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_START) {
                subsamplingAbility.resetStopped("LifecycleStateChanged:ON_START")
            } else if (event == Lifecycle.Event.ON_STOP) {
                subsamplingAbility.resetStopped("LifecycleStateChanged:ON_STOP")
            }
        }
    }
}