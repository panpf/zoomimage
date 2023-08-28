package com.github.panpf.zoomimage.view.subsampling

import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
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
    private val resetPausedLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            resetPaused("LifecycleStateChanged:ON_START")
        } else if (event == Lifecycle.Event.ON_STOP) {
            resetPaused("LifecycleStateChanged:ON_STOP")
        }
    }
    private val tileDrawHelper = TileDrawHelper(engine)


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
     * If true, subsampling is paused and loaded tiles are released, which will be reloaded after resumed
     */
    var paused: Boolean
        get() = engine.paused
        set(value) {
            engine.paused = value
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
    val tileList: List<TileSnapshot>
        get() = engine.tileList

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

        // At this time, view.findViewTreeLifecycleOwner() is not null
        setLifecycle(view.findViewTreeLifecycleOwner()?.lifecycle ?: view.context.findLifecycle())
    }

    /**
     * Set the lifecycle, which automatically controls pause and resume, which is obtained from View.findViewTreeLifecycleOwner() by default,
     * and can be set by this method if the default acquisition method is not applicable
     */
    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetPaused("setLifecycle")
        }
    }

    /**
     * Register a [tileList] property change listener
     */
    fun registerOnTileChangedListener(listener: OnTileChangeListener) =
        engine.registerOnTileChangedListener(listener)

    /**
     * Unregister a [tileList] property change listener
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
     * Register a [paused] property change listener
     */
    fun registerOnPauseChangeListener(listener: OnPauseChangeListener) =
        engine.registerOnPauseChangeListener(listener)

    /**
     * Unregister a [paused] property change listener
     */
    fun unregisterOnPauseChangeListener(listener: OnPauseChangeListener): Boolean =
        engine.unregisterOnPauseChangeListener(listener)

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
        resetPaused("onVisibilityChanged:$visibilityName")
    }


    /* *************************************** Internal ***************************************** */

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