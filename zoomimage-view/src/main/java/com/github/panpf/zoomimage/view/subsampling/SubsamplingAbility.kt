package com.github.panpf.zoomimage.view.subsampling

import android.graphics.Canvas
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.view.internal.getLifecycle
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.subsampling.internal.SubsamplingEngine
import com.github.panpf.zoomimage.view.subsampling.internal.TileDrawHelper

class SubsamplingAbility(private val view: View, logger: Logger) {

    val logger: Logger = logger.newLogger(module = "SubsamplingAbility")
    internal val engine: SubsamplingEngine = SubsamplingEngine(this.logger)
    private var lifecycle: Lifecycle? = null
    private var imageSource: ImageSource? = null
    private val resetPausedLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            resetPaused("LifecycleStateChanged:ON_START")
        } else if (event == Lifecycle.Event.ON_STOP) {
            resetPaused("LifecycleStateChanged:ON_STOP")
        }
    }
    private val tileDrawHelper = TileDrawHelper(engine)

    /* Configurable properties */
    var ignoreExifOrientation: Boolean
        get() = engine.ignoreExifOrientation
        set(value) {
            engine.ignoreExifOrientation = value
        }
    var showTileBounds = false
        set(value) {
            if (field != value) {
                field = value
                view.invalidate()
            }
        }
    var tileMemoryCache: TileMemoryCache?
        get() = engine.tileMemoryCache
        set(value) {
            engine.tileMemoryCache = value
        }
    var disableMemoryCache: Boolean
        get() = engine.disableMemoryCache
        set(value) {
            engine.disableMemoryCache = value
        }
    var tileBitmapPool: TileBitmapPool?
        get() = engine.tileBitmapPool
        set(value) {
            engine.tileBitmapPool = value
        }
    var disallowReuseBitmap: Boolean
        get() = engine.disallowReuseBitmap
        set(value) {
            engine.disallowReuseBitmap = value
        }
    var paused: Boolean
        get() = engine.paused
        set(value) {
            engine.paused = value
        }

    /* Information properties */
    val imageInfo: ImageInfo?
        get() = engine.imageInfo
    val ready: Boolean
        get() = engine.ready
    val tileList: List<TileSnapshot>
        get() = engine.tileList
    val imageLoadRect: IntRectCompat
        get() = engine.imageLoadRect

    init {
        engine.registerOnTileChangedListener {
            view.invalidate()
        }
    }


    /* ********************************* Interact with consumers ********************************* */

    fun setImageSource(imageSource: ImageSource?) {
        if (this.imageSource == imageSource) return
        this.imageSource = imageSource
        if (view.isAttachedToWindowCompat) {
            engine.setImageSource(imageSource)
        }

        // At this time, view.findViewTreeLifecycleOwner() is not null
        setLifecycle(view.findViewTreeLifecycleOwner()?.lifecycle ?: view.context.getLifecycle())
    }

    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetPaused("setLifecycle")
        }
    }

    fun registerOnTileChangedListener(listener: OnTileChangeListener) {
        engine.registerOnTileChangedListener(listener)
    }

    fun unregisterOnTileChangedListener(listener: OnTileChangeListener): Boolean {
        return engine.unregisterOnTileChangedListener(listener)
    }

    fun registerOnReadyChangeListener(listener: OnReadyChangeListener) {
        engine.registerOnReadyChangeListener(listener)
    }

    fun unregisterOnReadyChangeListener(listener: OnReadyChangeListener): Boolean {
        return engine.unregisterOnReadyChangeListener(listener)
    }

    fun registerOnPauseChangeListener(listener: OnPauseChangeListener) {
        engine.registerOnPauseChangeListener(listener)
    }

    fun unregisterOnPauseChangeListener(listener: OnPauseChangeListener): Boolean {
        return engine.unregisterOnPauseChangeListener(listener)
    }

    fun registerOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener) {
        engine.registerOnImageLoadRectChangeListener(listener)
    }

    fun unregisterOnImageLoadRectChangeListener(listener: OnImageLoadRectChangeListener): Boolean {
        return engine.unregisterOnImageLoadRectChangeListener(listener)
    }


    /* ********************************* Interact with View ********************************* */

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

    internal fun onVisibilityChanged(
        @Suppress("UNUSED_PARAMETER") changedView: View,
        visibility: Int
    ) {
        val visibilityName = when (visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }
        resetPaused("onVisibilityChanged:$visibilityName")
    }


    /* ********************************* Internal ********************************* */

    private fun resetPaused(caller: String) {
        val viewVisible = view.isVisible
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val paused = !viewVisible || !lifecycleStarted
        logger.d {
            "resetPaused:$caller. $paused. " +
                    "viewVisible=$viewVisible, " +
                    "lifecycleStarted=$lifecycleStarted. " +
                    "'${imageSource?.key}'"
        }
        engine.paused = paused
    }

    private fun registerLifecycleObserver() {
        if (view.isAttachedToWindowCompat) {
            lifecycle?.addObserver(resetPausedLifecycleObserver)
        }
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(resetPausedLifecycleObserver)
    }
}