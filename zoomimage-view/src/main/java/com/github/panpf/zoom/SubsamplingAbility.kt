package com.github.panpf.zoom

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.zoom.internal.Logger
import com.github.panpf.zoom.internal.SubsamplingEngine
import com.github.panpf.zoom.internal.Tile
import com.github.panpf.zoom.internal.getLifecycle
import com.github.panpf.zoom.internal.isAttachedToWindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("unused", "UNUSED_PARAMETER")
class SubsamplingAbility(
    private val view: View,
    logger: Logger,
    zoomAbility: ZoomAbility
) {

    companion object {
        private const val MODULE = "SubsamplingAbility"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val engine: SubsamplingEngine
    private var lifecycle: Lifecycle? = null
    private var imageSource: ImageSource? = null
    private val engineAutoPauseLifecycleObserver = EngineAutoPauseLifecycleObserver()
    private var lastPostResetSubsamplingHelperJob: Job? = null

    init {
        setLifecycle(view.context.getLifecycle())
        engine = SubsamplingEngine(view.context, logger, zoomAbility.engine)
    }


    /* ********************************* Interact with consumers ********************************* */

    fun setImageSource(imageSource: ImageSource?) {
        this.imageSource = imageSource
        engine.destroy()
        if (view.isAttachedToWindowCompat) {
            delayReset()
        }
    }

    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
        }
    }

    var showTileBounds: Boolean
        get() = engine.showTileBounds
        set(value) {
            engine.showTileBounds = value
        }

    var disallowMemoryCache: Boolean
        get() = engine.disallowMemoryCache
        set(value) {
            engine.disallowMemoryCache = value
        }

    var disallowReuseBitmap: Boolean
        get() = engine.disallowReuseBitmap
        set(value) {
            engine.disallowReuseBitmap = value
        }

    val tileList: List<Tile>?
        get() = engine.tileList

    fun eachTileList(action: (tile: Tile, load: Boolean) -> Unit) {
        engine.eachTileList(action)
    }

    fun addOnTileChangedListener(listener: OnTileChangedListener) {
        engine.addOnTileChangedListener(listener)
    }

    fun removeOnTileChangedListener(listener: OnTileChangedListener): Boolean {
        return engine.removeOnTileChangedListener(listener)
    }


    /* ********************************* Interact with View ********************************* */

    fun onAttachedToWindow() {
        delayReset()
        registerLifecycleObserver()
    }

    fun onDetachedFromWindow() {
        engine.destroy()
        unregisterLifecycleObserver()
    }

    fun onDraw(canvas: Canvas) {
        engine.onDraw(canvas)
    }

    fun onVisibilityChanged(changedView: View, visibility: Int) {
        engine.paused = visibility != View.VISIBLE
    }

    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        delayReset()
    }

    fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        engine.destroy()
        if (view.isAttachedToWindowCompat) {
            delayReset()
        }
    }


    /* ********************************* Internal ********************************* */

    private fun delayReset() {
        // Triggering the reset SubsamplingHelper frequently (such as changing the view size in shared element animations)
        // can cause large fluctuations in memory, so delayed resets can avoid this problem
        lastPostResetSubsamplingHelperJob?.cancel()
        lastPostResetSubsamplingHelperJob = scope.launch(Dispatchers.Main) {
            delay(60)
            engine.destroy()
            val imageSource = imageSource
            if (imageSource != null) {
                engine.setImageSource(imageSource)
            }
        }
    }

    private fun registerLifecycleObserver() {
        if (view.isAttachedToWindowCompat) {
            lifecycle?.addObserver(engineAutoPauseLifecycleObserver)
            engine.paused = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == false
        }
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(engineAutoPauseLifecycleObserver)
        engine.paused = false
    }

    private inner class EngineAutoPauseLifecycleObserver : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_START -> engine.paused = true
                Lifecycle.Event.ON_STOP -> engine.paused = false
                else -> {}
            }
        }
    }
}