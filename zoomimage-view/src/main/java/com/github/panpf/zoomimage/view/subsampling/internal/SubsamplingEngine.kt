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
package com.github.panpf.zoomimage.view.subsampling.internal

import android.view.View
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.internal.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsampling
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.subsampling.OnImageLoadRectChangeListener
import com.github.panpf.zoomimage.view.subsampling.OnPauseChangeListener
import com.github.panpf.zoomimage.view.subsampling.OnReadyChangeListener
import com.github.panpf.zoomimage.view.subsampling.OnTileChangeListener
import com.github.panpf.zoomimage.view.subsampling.TileSnapshot
import com.github.panpf.zoomimage.view.zoom.OnContainerSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SubsamplingEngine constructor(logger: Logger) {

    val logger: Logger = logger.newLogger(module = "SubsamplingEngine")
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var tileMemoryCacheHelper = TileMemoryCacheHelper(logger)
    private var tileBitmapPoolHelper = TileBitmapPoolHelper(logger)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var onTileChangeListenerList: MutableSet<OnTileChangeListener>? = null
    private var onReadyChangeListenerList: MutableSet<OnReadyChangeListener>? = null
    private var onPauseChangeListenerList: MutableSet<OnPauseChangeListener>? = null
    private var onImageLoadRectChangeListenerList: MutableSet<OnImageLoadRectChangeListener>? = null
    private var lastResetTileDecoderJob: Job? = null

    var containerSize: IntSizeCompat = IntSizeCompat.Zero
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("containerSizeChanged")
            }
        }
    var contentSize: IntSizeCompat = IntSizeCompat.Zero
        set(value) {
            if (field != value) {
                field = value
                resetTileDecoder("contentSizeChanged")
            }
        }
    var ignoreExifOrientation: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resetTileManager("ignoreExifOrientationChanged")
            }
        }
    var tileMemoryCache: TileMemoryCache?
        get() = tileMemoryCacheHelper.tileMemoryCache
        set(value) {
            tileMemoryCacheHelper.tileMemoryCache = value
        }
    var disableMemoryCache: Boolean
        get() = tileMemoryCacheHelper.disableMemoryCache
        set(value) {
            tileMemoryCacheHelper.disableMemoryCache = value
        }
    var tileBitmapPool: TileBitmapPool?
        get() = tileBitmapPoolHelper.tileBitmapPool
        set(value) {
            tileBitmapPoolHelper.tileBitmapPool = value
        }
    var disallowReuseBitmap: Boolean
        get() = tileBitmapPoolHelper.disallowReuseBitmap
        set(value) {
            tileBitmapPoolHelper.disallowReuseBitmap = value
        }
    var showTileBounds = false // todo 从这里移出
        set(value) {
            if (field != value) {
                field = value
                notifyTileChange()
            }
        }

    var imageKey: String? = null
        private set
    val ready: Boolean
        get() = imageInfo != null && tileDecoder != null && tileManager != null
    var imageInfo: ImageInfo? = null
        private set
    var tileList: List<TileSnapshot> = emptyList()
        private set
    val imageLoadRect: IntRectCompat
        get() = tileManager?.imageLoadRect ?: IntRectCompat.Zero
    var paused = false
        set(value) {
            if (field != value) {
                field = value
                notifyPauseChange()
            }
        }

    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${imageSource?.key}'" }
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        imageKey = imageSource?.key
        resetTileDecoder("setImageSource")
        return true
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val ignoreExifOrientation = ignoreExifOrientation

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
            val imageInfoResult = imageSource.readImageInfo(ignoreExifOrientation)
            val imageInfo = imageInfoResult.getOrNull()
            this@SubsamplingEngine.imageInfo = imageInfo
            val canUseSubsamplingResult = imageInfo?.let { canUseSubsampling(it, contentSize) }
            if (imageInfo != null && canUseSubsamplingResult == 0) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageKey}'"
                }
                this@SubsamplingEngine.tileDecoder = TileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    tileBitmapPoolHelper = tileBitmapPoolHelper,
                    imageInfo = imageInfo,
                )
                resetTileManager(caller)
            } else {
                val cause = when {
                    imageInfo == null -> imageInfoResult.exceptionOrNull()!!.message
                    canUseSubsamplingResult == -1 -> "The content size is greater than or equal to the original image"
                    canUseSubsamplingResult == -2 -> "The content aspect ratio is different with the original image"
                    canUseSubsamplingResult == -3 -> "Image type not support subsampling"
                    else -> "Unknown canUseSubsamplingResult: $canUseSubsamplingResult"
                }
                val level = if (canUseSubsamplingResult == -1) Logger.DEBUG else Logger.ERROR
                val type = if (canUseSubsamplingResult == -1) "skipped" else "failed"
                logger.log(level) {
                    "resetTileDecoder:$caller. $type, $cause. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo: ${imageInfo?.toShortString()}. " +
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
                tileList = manager.tileList.map { tile ->
                    TileSnapshot(
                        srcRect = tile.srcRect,
                        inSampleSize = tile.inSampleSize,
                        bitmap = tile.bitmap,
                        state = tile.state,
                    )
                }
                notifyTileChange()
            },
            onImageLoadRectChanged = {
                notifyImageLoadRectChange()
            }
        )
        logger.d {
            val tileMaxSize = tileManager.tileMaxSize
            val tileMap = tileManager.tileMap
            val tileMapInfoList = tileMap.keys.sortedDescending()
                .map { "${it}:${tileMap[it]?.size}" }
            "resetTileManager:$caller. success. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "tileMaxSize=${tileMaxSize.toShortString()}, " +
                    "tileMap=$tileMapInfoList, " +
                    "'${imageKey}'"
        }
        this@SubsamplingEngine.tileManager = tileManager
        notifyReadyChange()
        notifyTileChange()
    }

    fun refreshTiles(
        contentVisibleRect: IntRectCompat,
        scale: Float,
        rotation: Int,
        transforming: Boolean,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (paused) {
            logger.d { "refreshTiles:$caller. interrupted, paused. '${imageKey}'" }
            tileManager.clean("refreshTiles:paused")
            return
        }
        tileManager.refreshTiles(
            contentVisibleRect = contentVisibleRect,
            scale = scale,
            rotation = rotation,
            transforming = transforming,
            caller = caller
        )
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
            notifyReadyChange()
            notifyTileChange()
        }
    }

    fun registerOnTileChangedListener(listener: OnTileChangeListener) {
        this.onTileChangeListenerList = (onTileChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun unregisterOnTileChangedListener(listener: OnTileChangeListener): Boolean {
        return onTileChangeListenerList?.remove(listener) == true
    }

    fun registerOnReadyChangeListener(listener: OnReadyChangeListener) {
        this.onReadyChangeListenerList = (onReadyChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun unregisterOnReadyChangeListener(listener: OnReadyChangeListener): Boolean {
        return onReadyChangeListenerList?.remove(listener) == true
    }

    fun registerOnPauseChangeListener(listener: OnPauseChangeListener) {
        this.onPauseChangeListenerList = (onPauseChangeListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
    }

    fun unregisterOnPauseChangeListener(listener: OnPauseChangeListener): Boolean {
        return onPauseChangeListenerList?.remove(listener) == true
    }

    fun registerOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener) {
        this.onImageLoadRectChangeListenerList =
            (onImageLoadRectChangeListenerList ?: LinkedHashSet()).apply {
                add(listener)
            }
    }

    fun unregisterOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener): Boolean {
        return onImageLoadRectChangeListenerList?.remove(listener) == true
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

    private fun notifyPauseChange() {
        val paused = paused
        onPauseChangeListenerList?.forEach {
            it.onPauseChanged(paused)
        }
    }

    private fun notifyImageLoadRectChange() {
        val imageLoadRect = imageLoadRect
        onImageLoadRectChangeListenerList?.forEach {
            it.onImageLoadRectChanged(imageLoadRect)
        }
    }
}

fun bindZoomAndSubsampling(
    view: View,
    zoomEngine: ZoomEngine,
    subsamplingEngine: SubsamplingEngine
) {
    subsamplingEngine.containerSize = zoomEngine.containerSize
    zoomEngine.registerOnContainerSizeChangeListener(
        OnContainerSizeChangeListenerImpl(subsamplingEngine)
    )

    subsamplingEngine.contentSize = zoomEngine.contentSize
    zoomEngine.registerOnContentSizeChangeListener {
        subsamplingEngine.contentSize = zoomEngine.contentSize
    }

    zoomEngine.registerOnTransformChangeListener {
        subsamplingEngine.refreshTiles(
            contentVisibleRect = zoomEngine.contentVisibleRect,
            scale = zoomEngine.transform.scaleX,
            rotation = zoomEngine.transform.rotation.roundToInt(),
            transforming = zoomEngine.transforming,
            caller = "transformChanged"
        )
    }

    subsamplingEngine.registerOnReadyChangeListener {
        val imageInfo = subsamplingEngine.imageInfo
        zoomEngine.contentOriginSize = if (subsamplingEngine.ready && imageInfo != null) {
            imageInfo.size
        } else {
            IntSizeCompat.Zero
        }
        subsamplingEngine.refreshTiles(
            contentVisibleRect = zoomEngine.contentVisibleRect,
            scale = zoomEngine.transform.scaleX,
            rotation = zoomEngine.transform.rotation.roundToInt(),
            transforming = zoomEngine.transforming,
            caller = "readyChanged"
        )
    }

    subsamplingEngine.registerOnPauseChangeListener {
        subsamplingEngine.refreshTiles(
            contentVisibleRect = zoomEngine.contentVisibleRect,
            scale = zoomEngine.transform.scaleX,
            rotation = zoomEngine.transform.rotation.roundToInt(),
            transforming = zoomEngine.transforming,
            caller = if (it) "paused" else "resumed"
        )
    }

    subsamplingEngine.registerOnTileChangedListener {
        view.invalidate()
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