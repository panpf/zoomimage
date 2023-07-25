package com.github.panpf.zoomimage.view.subsampling

import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.view.internal.getLifecycle
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.subsampling.internal.SubsamplingEngine

class SubsamplingAbility(private val view: View, logger: Logger) {

    private val logger: Logger = logger.newLogger(module = "SubsamplingAbility")
    private var lifecycle: Lifecycle? = null
    private var imageSource: ImageSource? = null
    private val resetPausedLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            resetPaused("LifecycleStateChanged:ON_START")
        } else if (event == Lifecycle.Event.ON_STOP) {
            resetPaused("LifecycleStateChanged:ON_STOP")
        }
    }
    internal val engine: SubsamplingEngine = SubsamplingEngine(this.logger)

    init {
        setLifecycle(view.context.getLifecycle())
    }


    /* ********************************* Interact with consumers ********************************* */

    fun setImageSource(imageSource: ImageSource?) {
        if (this.imageSource == imageSource) return
        this.imageSource = imageSource
        if (view.isAttachedToWindowCompat) {
            engine.setImageSource(imageSource)
        }
    }

    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetPaused("setLifecycle")
        }
    }

    var ignoreExifOrientation: Boolean
        get() = engine.ignoreExifOrientation
        set(value) {
            engine.ignoreExifOrientation = value
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
    var showTileBounds: Boolean
        get() = engine.showTileBounds
        set(value) {
            engine.showTileBounds = value
        }

    val ready: Boolean
        get() = engine.ready
    val imageInfo: ImageInfo?
        get() = engine.imageInfo
    val tileList: List<Tile>?
        get() = engine.tileList
    val imageVisibleRect: IntRectCompat
        get() = engine.imageVisibleRect
    val imageLoadRect: IntRectCompat
        get() = engine.imageLoadRect

    var paused: Boolean
        get() = engine.paused
        set(value) {
            engine.paused = value
        }

    fun addOnTileChangedListener(listener: OnTileChangeListener) {
        engine.addOnTileChangedListener(listener)
    }

    fun removeOnTileChangedListener(listener: OnTileChangeListener): Boolean {
        return engine.removeOnTileChangedListener(listener)
    }

    fun addOnReadyChangeListener(listener: OnReadyChangeListener) {
        engine.addOnReadyChangeListener(listener)
    }

    fun removeOnReadyChangeListener(listener: OnReadyChangeListener): Boolean {
        return engine.removeOnReadyChangeListener(listener)
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

    internal fun onDraw(canvas: Canvas, displayMatrix: Matrix) {
        engine.drawTiles(canvas, displayMatrix)
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