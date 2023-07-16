package com.github.panpf.zoomimage

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.core.IntRectCompat
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.view.internal.SubsamplingEngine
import com.github.panpf.zoomimage.view.internal.getLifecycle
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("unused", "UNUSED_PARAMETER")
class SubsamplingAbility(
    private val view: View,
    private val zoomAbility: ZoomAbility    // todo 不在传入，而是外部绑定
) {

    val logger: Logger = zoomAbility.logger.newLogger(module = "SubsamplingAbility")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val engine: SubsamplingEngine =
        SubsamplingEngine(view.context, logger, zoomAbility.engine)
    private var lifecycle: Lifecycle? = null
    private var imageSource: ImageSource? = null
    private var initEngineJob: Job? = null
    private val setupImageSourceChannel = Channel<ImageSource>()
    private val resetPausedLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            resetPaused("LifecycleStateChanged:ON_START")
        } else if (event == Lifecycle.Event.ON_STOP) {
            resetPaused("LifecycleStateChanged:ON_STOP")
        }
    }

    init {
        setLifecycle(view.context.getLifecycle())
    }


    /* ********************************* Interact with consumers ********************************* */

    fun setImageSource(imageSource: ImageSource?) {
        if (this.imageSource != imageSource) {
            engine.destroy()
            this.imageSource = imageSource
            if (imageSource != null && view.isAttachedToWindowCompat) {
                initEngine()
            }
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

    var showTileBounds: Boolean
        get() = engine.showTileBounds
        set(value) {
            engine.showTileBounds = value
        }

    var disableMemoryCache: Boolean
        get() = engine.disableMemoryCache
        set(value) {
            engine.disableMemoryCache = value
        }

    var disallowReuseBitmap: Boolean
        get() = engine.disallowReuseBitmap
        set(value) {
            engine.disallowReuseBitmap = value
        }

    var ignoreExifOrientation: Boolean
        get() = engine.ignoreExifOrientation
        set(value) {
            engine.ignoreExifOrientation = value
        }

    var tileBitmapPool: TileBitmapPool?
        get() = engine.tileBitmapPool
        set(value) {
            engine.tileBitmapPool = value
        }

    var tileMemoryCache: TileMemoryCache?
        get() = engine.tileMemoryCache
        set(value) {
            engine.tileMemoryCache = value
        }

    val tileList: List<Tile>?
        get() = engine.tileList

    val imageVisibleRect: IntRectCompat
        get() = engine.imageVisibleRect

    val imageLoadRect: IntRectCompat
        get() = engine.imageLoadRect

    val imageSize: IntSizeCompat?
        get() = engine.imageSize

    val imageMimeType: String?
        get() = engine.imageMimeType

    val imageExifOrientation: Int?
        get() = engine.imageExifOrientation

    fun addOnTileChangedListener(listener: OnTileChangedListener) {
        engine.addOnTileChangedListener(listener)
    }

    fun removeOnTileChangedListener(listener: OnTileChangedListener): Boolean {
        return engine.removeOnTileChangedListener(listener)
    }


    /* ********************************* Interact with View ********************************* */

    fun onAttachedToWindow() {
        initEngine()
        registerLifecycleObserver()
    }

    fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        engine.destroy()
    }

    fun onDetachedFromWindow() {
        engine.destroy()
        unregisterLifecycleObserver()
    }

    fun onDraw(canvas: Canvas) {
        engine.onDraw(canvas)
    }

    fun onVisibilityChanged(changedView: View, visibility: Int) {
        val visibilityName = when (visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }
        resetPaused("onVisibilityChanged:$visibilityName")
    }

    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        initEngine()
    }


    /* ********************************* Internal ********************************* */

    private fun initEngine() {
        // Triggering the reset SubsamplingHelper frequently (such as changing the view size in shared element animations)
        // can cause large fluctuations in memory, so delayed resets can avoid this problem
        initEngineJob?.cancel()
        val imageSource = this@SubsamplingAbility.imageSource
        if (imageSource != null) {
            initEngineJob = scope.launch(Dispatchers.Main) {
                delay(60)
                engine.destroy()
                val imageSource1 = this@SubsamplingAbility.imageSource
                if (imageSource1 != null) {
                    engine.setImageSource(imageSource1)
                }
                initEngineJob = null
            }
        }
    }

    private fun resetPaused(caller: String) {
        val viewVisible = view.isVisible
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val paused = !viewVisible || !lifecycleStarted
        zoomAbility.logger.d { "resetPaused. $paused. $caller. " +
                "viewVisible=$viewVisible, " +
                "lifecycleStarted=$lifecycleStarted. " +
                "'${imageSource?.key}'" }
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