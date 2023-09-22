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

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.CreateTileDecoderException
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileManager.Companion.DefaultPausedContinuousTransformType
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.createTileDecoder
import com.github.panpf.zoomimage.subsampling.toIntroString
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.internal.findLifecycle
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.zoom.OnContainerSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Engines that control subsampling
 */
class SubsamplingEngine constructor(logger: Logger, private val view: View) {

    private val logger: Logger = logger.newLogger(module = "SubsamplingEngine")
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var tileMemoryCacheHelper = TileMemoryCacheHelper(this.logger)
    private var tileBitmapPoolHelper = TileBitmapPoolHelper(this.logger)
    private var onTileChangeListenerList: MutableSet<OnTileChangeListener>? = null
    private var onReadyChangeListenerList: MutableSet<OnReadyChangeListener>? = null
    private var onSampleSizeChangeListenerList: MutableSet<OnSampleSizeChangeListener>? = null
    private var onStoppedChangeListenerList: MutableSet<OnStoppedChangeListener>? = null
    private var onImageLoadRectChangeListenerList: MutableSet<OnImageLoadRectChangeListener>? = null
    private var lastResetTileDecoderJob: Job? = null
    private var lifecycle: Lifecycle? = null
    private val resetStoppedLifecycleObserver by lazy { ResetStoppedLifecycleObserver(this) }
    internal var imageKey: String? = null
        private set

    internal var containerSize: IntSizeCompat = IntSizeCompat.Zero
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("containerSizeChanged")
            }
        }
    internal var contentSize: IntSizeCompat = IntSizeCompat.Zero
        set(value) {
            if (field != value) {
                field = value
                resetTileDecoder("contentSizeChanged")
            }
        }


    /* *********************************** Configurable properties ****************************** */

    /**
     * If true, the Exif rotation information for the image is ignored
     */
    var ignoreExifOrientation: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("ignoreExifOrientationChanged")
            }
        }

    /**
     * Set up the tile memory cache container
     */
    var tileMemoryCache: TileMemoryCache?
        get() = tileMemoryCacheHelper.tileMemoryCache
        set(value) {
            tileMemoryCacheHelper.tileMemoryCache = value
        }

    /**
     * If true, disable memory cache
     */
    var disableMemoryCache: Boolean
        get() = tileMemoryCacheHelper.disableMemoryCache
        set(value) {
            tileMemoryCacheHelper.disableMemoryCache = value
        }

    /**
     * Set up a shared Bitmap pool for the tile
     */
    var tileBitmapPool: TileBitmapPool?
        get() = tileBitmapPoolHelper.tileBitmapPool
        set(value) {
            tileBitmapPoolHelper.tileBitmapPool = value
        }

    /**
     * If true, Bitmap reuse is disabled
     */
    var disallowReuseBitmap: Boolean
        get() = tileBitmapPoolHelper.disallowReuseBitmap
        set(value) {
            tileBitmapPoolHelper.disallowReuseBitmap = value
        }

    /**
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec = TileAnimationSpec.Default
        set(value) {
            if (field != value) {
                field = value
                tileManager?.tileAnimationSpec = value
            }
        }

    /**
     * A continuous transform type that needs to pause loading
     */
    var pausedContinuousTransformType: Int = DefaultPausedContinuousTransformType
        set(value) {
            field = value
            tileManager?.pausedContinuousTransformType = value
        }

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    var disabledBackgroundTiles: Boolean = false
        set(value) {
            field = value
            tileManager?.disabledBackgroundTiles = value
        }

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    var stopped = false
        set(value) {
            if (field != value) {
                field = value
                notifyStopChange()
            }
        }

    /**
     * If true, the bounds of each tile is displayed
     */
    var showTileBounds = false
        set(value) {
            if (field != value) {
                field = value
                notifyTileChange()
            }
        }


    /* *********************************** Information properties ******************************* */

    /**
     * The information of the image, including width, height, format, exif information, etc
     */
    var imageInfo: ImageInfo? = null
        private set

    /**
     * Whether the image is ready for subsampling
     */
    val ready: Boolean
        get() = imageInfo != null && tileDecoder != null && tileManager != null

    /**
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    var foregroundTiles: List<TileSnapshot> = emptyList()
        private set

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    var backgroundTiles: List<TileSnapshot> = emptyList()
        private set

    /**
     * The sample size of the image
     */
    var sampleSize: Int = 0
        private set

    /**
     * The image load rect
     */
    var imageLoadRect: IntRectCompat = IntRectCompat.Zero
        private set

    /**
     * Tile grid size map, key is sample size, value is tile grid size
     */
    var tileGridSizeMap: Map<Int, IntOffsetCompat> = emptyMap()
        private set


    init {
        view.post {
            if (view.isAttachedToWindowCompat) {
                val lifecycle1 =
                    view.findViewTreeLifecycleOwner()?.lifecycle ?: view.context.findLifecycle()
                setLifecycle(lifecycle1)
            }
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                reset("onViewAttachedToWindow")
                registerLifecycleObserver()
            }

            override fun onViewDetachedFromWindow(v: View) {
                clean("onViewDetachedFromWindow")
                unregisterLifecycleObserver()
            }
        })
    }

    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set up an image source from which image tile are loaded
     */
    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${this.imageSource?.key}' -> '${imageSource?.key}'" }
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        imageKey = imageSource?.key
        if (view.isAttachedToWindowCompat) {
            reset("setImageSource")
        }
        return true
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
     * Register a [backgroundTiles] or [foregroundTiles] property change listener
     */
    fun registerOnTileChangeListener(listener: OnTileChangeListener) {
        this.onTileChangeListenerList = (onTileChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    /**
     * Unregister a [backgroundTiles] or [foregroundTiles] property change listener
     */
    fun unregisterOnTileChangeListener(listener: OnTileChangeListener): Boolean {
        return onTileChangeListenerList?.remove(listener) == true
    }

    /**
     * Register a [ready] property change listener
     */
    fun registerOnReadyChangeListener(listener: OnReadyChangeListener) {
        this.onReadyChangeListenerList = (onReadyChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    /**
     * Unregister a [ready] property change listener
     */
    fun unregisterOnReadyChangeListener(listener: OnReadyChangeListener): Boolean {
        return onReadyChangeListenerList?.remove(listener) == true
    }

    /**
     * Register a [sampleSize] property change listener
     */
    fun registerOnSampleSizeChangeListener(listener: OnSampleSizeChangeListener) {
        this.onSampleSizeChangeListenerList =
            (onSampleSizeChangeListenerList ?: LinkedHashSet()).apply {
                add(listener)
            }
    }

    /**
     * Unregister a [sampleSize] property change listener
     */
    fun unregisterOnSampleSizeChangeListener(listener: OnSampleSizeChangeListener): Boolean {
        return onSampleSizeChangeListenerList?.remove(listener) == true
    }

    /**
     * Register a [stopped] property change listener
     */
    fun registerOnStoppedChangeListener(listener: OnStoppedChangeListener) {
        this.onStoppedChangeListenerList = (onStoppedChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    /**
     * Unregister a [stopped] property change listener
     */
    fun unregisterOnStoppedChangeListener(listener: OnStoppedChangeListener): Boolean {
        return onStoppedChangeListenerList?.remove(listener) == true
    }

    /**
     * Register a [imageLoadRect] property change listener
     */
    fun registerOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener) {
        this.onImageLoadRectChangeListenerList =
            (onImageLoadRectChangeListenerList ?: LinkedHashSet()).apply {
                add(listener)
            }
    }

    /**
     * Unregister a [imageLoadRect] property change listener
     */
    fun unregisterOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener): Boolean {
        return onImageLoadRectChangeListenerList?.remove(listener) == true
    }


    /* *************************************** Internal ***************************************** */

    internal fun bindZoomEngine(zoomableEngine: ZoomableEngine) {
        containerSize = zoomableEngine.containerSize
        zoomableEngine.registerOnContainerSizeChangeListener(
            OnContainerSizeChangeListenerImpl(this@SubsamplingEngine)
        )

        contentSize = zoomableEngine.contentSize
        zoomableEngine.registerOnContentSizeChangeListener {
            contentSize = zoomableEngine.contentSize
        }

        val refreshTiles: (caller: String) -> Unit = { caller ->
            val transform = zoomableEngine.transformState.value
            refreshTiles(
                contentVisibleRect = zoomableEngine.contentVisibleRectState.value,
                scale = transform.scaleX,
                rotation = transform.rotation.roundToInt(),
                continuousTransformType = zoomableEngine.continuousTransformTypeState.value,
                caller = caller
            )
        }

        coroutineScope.launch {
            zoomableEngine.transformState.collect {
                refreshTiles("transformChanged")
            }
        }
        coroutineScope.launch {
            zoomableEngine.continuousTransformTypeState.collect {
                refreshTiles("continuousTransformTypeChanged")
            }
        }

        registerOnReadyChangeListener {
            val imageInfo = imageInfo
            zoomableEngine.contentOriginSize = if (ready && imageInfo != null) {
                imageInfo.size
            } else {
                IntSizeCompat.Zero
            }
            refreshTiles("readyChanged")
        }
        registerOnStoppedChangeListener {
            refreshTiles(if (it) "stopped" else "started")
        }
    }

    private fun refreshTiles(
        contentVisibleRect: IntRectCompat,
        scale: Float,
        rotation: Int,
        @ContinuousTransformType continuousTransformType: Int,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (stopped) {
            logger.d { "refreshTiles:$caller. interrupted, stopped. '${imageKey}'" }
            tileManager.clean("refreshTiles:stopped")
            return
        }
        tileManager.refreshTiles(
            scale = scale,
            contentVisibleRect = contentVisibleRect,
            rotation = rotation,
            continuousTransformType = continuousTransformType,
            caller = caller
        )
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val ignoreExifOrientation = ignoreExifOrientation

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                createTileDecoder(
                    logger = logger,
                    tileBitmapPoolHelper = tileBitmapPoolHelper,
                    imageSource = imageSource,
                    thumbnailSize = contentSize,
                    ignoreExifOrientation = ignoreExifOrientation
                )
            }
            val newTileDecoder = result.getOrNull()
            if (newTileDecoder != null) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${newTileDecoder.imageInfo.toShortString()}. " +
                            "'${imageKey}'"
                }
                this@SubsamplingEngine.tileDecoder = newTileDecoder
                this@SubsamplingEngine.imageInfo = newTileDecoder.imageInfo
                resetTileManager(caller)
            } else {
                val exception = result.exceptionOrNull()!! as CreateTileDecoderException
                this@SubsamplingEngine.imageInfo = exception.imageInfo
                val level = if (exception.skipped) Logger.DEBUG else Logger.ERROR
                val type = if (exception.skipped) "skipped" else "error"
                logger.log(level) {
                    "resetTileDecoder:$caller. $type, ${exception.message}. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo: ${exception.imageInfo?.toShortString()}. " +
                            "'${imageKey}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager(caller)

        val imageSource = imageSource ?: return
        val tileDecoder = tileDecoder ?: return
        val imageInfo = imageInfo ?: return
        val containerSize = containerSize.takeIf { !it.isEmpty() } ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            imageSource = imageSource,
            containerSize = containerSize,
            contentSize = contentSize,
            tileMemoryCacheHelper = tileMemoryCacheHelper,
            tileBitmapPoolHelper = tileBitmapPoolHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                backgroundTiles = manager.backgroundTiles
                foregroundTiles = manager.foregroundTiles
                notifyTileChange()
            },
            onSampleSizeChanged = { manager ->
                sampleSize = manager.sampleSize
                notifySampleSizeChange()
            },
            onImageLoadRectChanged = { manager ->
                imageLoadRect = manager.imageLoadRect
                notifyImageLoadRectChange()
            }
        ).apply {
            pausedContinuousTransformType = this@SubsamplingEngine.pausedContinuousTransformType
            disabledBackgroundTiles = this@SubsamplingEngine.disabledBackgroundTiles
            tileAnimationSpec = this@SubsamplingEngine.tileAnimationSpec
        }
        tileGridSizeMap = tileManager.sortedTileGridMap.mapValues { entry ->
            entry.value.last().coordinate.let { IntOffsetCompat(it.x + 1, it.y + 1) }
        }
        logger.d {
            "resetTileManager:$caller. success. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "preferredTileSize=${tileManager.preferredTileSize.toShortString()}, " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${imageKey}'"
        }
        this@SubsamplingEngine.tileManager = tileManager
        notifyReadyChange()
        notifyTileChange()
    }

    private fun cleanTileDecoder(caller: String) {
        val lastResetTileDecoderJob = this@SubsamplingEngine.lastResetTileDecoderJob
        if (lastResetTileDecoderJob != null) {
            lastResetTileDecoderJob.cancel("cleanTileDecoder:$caller")
            this@SubsamplingEngine.lastResetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingEngine.tileDecoder
        if (tileDecoder != null) {
            logger.d { "cleanTileDecoder:$caller. '${imageKey}'" }
            tileDecoder.destroy("cleanTileDecoder:$caller")
            this@SubsamplingEngine.tileDecoder = null
            notifyReadyChange()
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingEngine.tileManager
        if (tileManager != null) {
            logger.d { "cleanTileManager:$caller. '${imageKey}'" }
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingEngine.tileManager = null
            tileGridSizeMap = emptyMap()
            foregroundTiles = emptyList()
            backgroundTiles = emptyList()
            sampleSize = 0
            imageLoadRect = IntRectCompat.Zero
            notifyReadyChange()
            notifyTileChange()
        }
    }

    private fun notifyTileChange() {
        onTileChangeListenerList?.forEach {
            it.onTileChanged()
        }
    }

    private fun notifyReadyChange() {
        val ready = ready
        onReadyChangeListenerList?.forEach {
            it.onReadyChanged(ready)
        }
    }

    private fun notifyStopChange() {
        val stopped = stopped
        onStoppedChangeListenerList?.forEach {
            it.onStoppedChanged(stopped)
        }
    }

    private fun notifySampleSizeChange() {
        val sampleSize = sampleSize
        onSampleSizeChangeListenerList?.forEach {
            it.onSampleSizeChanged(sampleSize)
        }
    }

    private fun notifyImageLoadRectChange() {
        val imageLoadRect = imageLoadRect
        onImageLoadRectChangeListenerList?.forEach {
            it.onImageLoadRectChanged(imageLoadRect)
        }
    }

    private fun reset(caller: String) {
        clean("reset:$caller")
        resetTileDecoder("destroy:$caller")
    }

    private fun clean(caller: String) {
        cleanTileDecoder("clean:$caller")
        cleanTileManager("clean:$caller")
    }


    internal fun resetStopped(caller: String) {
        val viewVisible = view.isVisible
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val stopped = !viewVisible || !lifecycleStarted
        logger.d {
            "resetStopped:$caller. $stopped. " +
                    "viewVisible=$viewVisible, " +
                    "lifecycleStarted=$lifecycleStarted. " +
                    "'${imageSource?.key}'"
        }
        this.stopped = stopped
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
        val engine: SubsamplingEngine
    ) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_START) {
                engine.resetStopped("LifecycleStateChanged:ON_START")
            } else if (event == Lifecycle.Event.ON_STOP) {
                engine.resetStopped("LifecycleStateChanged:ON_STOP")
            }
        }
    }

    private class OnContainerSizeChangeListenerImpl(
        private val subsamplingEngine: SubsamplingEngine
    ) : OnContainerSizeChangeListener {

        private var lastDelayJob: Job? = null
        private val scope = CoroutineScope(Dispatchers.Main.immediate)

        override fun onContainerSizeChanged(containerSize: IntSizeCompat) {
            // Changes in viewSize cause a large chain reaction that can cause large memory fluctuations.
            // View size animations cause frequent changes in viewSize, so a delayed reset avoids this problem
            lastDelayJob?.cancel()
            lastDelayJob = scope.launch(Dispatchers.Main) {
                delay(60)
                lastDelayJob = null
                subsamplingEngine.containerSize = containerSize
            }
        }
    }
}