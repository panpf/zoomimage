package com.github.panpf.zoom

import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.sketch.cache.CachePolicy
import com.github.panpf.sketch.decode.internal.ImageFormat
import com.github.panpf.sketch.decode.internal.supportBitmapRegionDecoder
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.sketch
import com.github.panpf.sketch.stateimage.internal.SketchStateDrawable
import com.github.panpf.sketch.util.SketchUtils
import com.github.panpf.sketch.util.findLastSketchDrawable
import com.github.panpf.sketch.util.getLastChildDrawable
import com.github.panpf.zoom.internal.ImageViewBridge
import com.github.panpf.zoom.internal.SubsamplingHelper
import com.github.panpf.zoom.internal.canUseSubsampling
import com.github.panpf.zoom.internal.contentSize
import com.github.panpf.zoom.internal.getLifecycle
import com.github.panpf.zoom.internal.isAttachedToWindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SubsamplingAbility(
    val view: View,
    val imageViewBridge: ImageViewBridge,
    val zoomAbility: ZoomAbility
) {

    companion object {
        private const val MODULE = "ZoomAbility"
    }

    private var subsamplingHelper: SubsamplingHelper? = null
    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                subsamplingHelper?.paused = true
            }

            Lifecycle.Event.ON_STOP -> {
                subsamplingHelper?.paused = false
            }

            else -> {}
        }
    }
    private var onTileChangedListenerList: MutableSet<OnTileChangedListener>? = null
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
    private var lastPostResetSubsamplingHelperJob: Job? = null
    var lifecycle: Lifecycle? = null
        set(value) {
            if (value != field) {
                unregisterLifecycleObserver()
                field = value
                registerLifecycleObserver()
            }
        }
    var showTileBounds: Boolean = false
        set(value) {
            field = value
            subsamplingHelper?.showTileBounds = value
        }

    val tileList: List<Tile>?
        get() = subsamplingHelper?.tileList

    init {
        this.lifecycle = view.context.getLifecycle()
    }

    fun onAttachedToWindow() {
        initialize()
        registerLifecycleObserver()
    }

    fun onDetachedFromWindow() {
        destroy()
        unregisterLifecycleObserver()
    }

    fun onDraw(canvas: Canvas) {
        subsamplingHelper?.onDraw(canvas)
    }

    fun onVisibilityChanged(changedView: View, visibility: Int) {
        subsamplingHelper?.paused = visibility != View.VISIBLE
    }

    fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        postDelayResetSubsamplingHelper()
    }

    private fun registerLifecycleObserver() {
        if (view?.isAttachedToWindowCompat == true) {
            lifecycle?.addObserver(lifecycleObserver)
            subsamplingHelper?.paused =
                lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == false
        }
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(lifecycleObserver)
        subsamplingHelper?.paused = false
    }

    private fun destroy() {
        subsamplingHelper?.destroy()
        subsamplingHelper = null
    }

    private fun postDelayResetSubsamplingHelper() {
        // Triggering the reset SubsamplingHelper frequently (such as changing the view size in shared element animations)
        // can cause large fluctuations in memory, so delayed resets can avoid this problem
        lastPostResetSubsamplingHelperJob?.cancel()
        lastPostResetSubsamplingHelperJob = scope.launch(Dispatchers.Main) {
            delay(60)
            subsamplingHelper?.destroy()
            subsamplingHelper = newSubsamplingHelper()
        }
    }

    fun onDrawableChanged(oldDrawable: Drawable?, newDrawable: Drawable?) {
        val imageView = view ?: return
        destroy()
        if (imageView.isAttachedToWindowCompat) {
            initialize()
        }
    }

    private fun initialize() {
        postDelayResetSubsamplingHelper()
    }

    private fun newSubsamplingHelper(): SubsamplingHelper? {
        val zoomAbility = zoomAbility ?: return null
        val imageView = view ?: return null
        val imageViewSuperBridge = imageViewBridge ?: return null
        val sketch = imageView.context.sketch
        val logger = sketch.logger

        val viewContentSize = imageView.contentSize
        if (viewContentSize == null) {
            logger.d(MODULE) { "Can't use Subsampling. View size error" }
            return null
        }

        val drawable = imageViewSuperBridge.getDrawable()
        if (drawable == null) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is null" }
            return null
        }
        if (drawable.getLastChildDrawable() is SketchStateDrawable) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is StateDrawable" }
            return null
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        val sketchDrawable = drawable.findLastSketchDrawable()
        if (sketchDrawable == null) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is not SketchDrawable" }
            return null
        }
        if (sketchDrawable is Animatable) {
            logger.d(MODULE) { "Can't use Subsampling. Drawable is Animatable" }
            return null
        }
        val imageWidth = sketchDrawable.imageInfo.width
        val imageHeight = sketchDrawable.imageInfo.height
        val mimeType = sketchDrawable.imageInfo.mimeType
        val requestKey = sketchDrawable.requestKey

        if (drawableWidth >= imageWidth && drawableHeight >= imageHeight) {
            logger.d(MODULE) {
                "Don't need to use Subsampling. drawableSize: %dx%d, imageSize: %dx%d, mimeType: %s. '%s'"
                    .format(
                        drawableWidth,
                        drawableHeight,
                        imageWidth,
                        imageHeight,
                        mimeType,
                        requestKey
                    )
            }
            return null
        }
        if (!canUseSubsampling(imageWidth, imageHeight, drawableWidth, drawableHeight)) {
            logger.d(MODULE) {
                "Can't use Subsampling. drawableSize error. drawableSize: %dx%d, imageSize: %dx%d, mimeType: %s. '%s'"
                    .format(
                        drawableWidth,
                        drawableHeight,
                        imageWidth,
                        imageHeight,
                        mimeType,
                        requestKey
                    )
            }
            return null
        }
        if (ImageFormat.parseMimeType(mimeType)?.supportBitmapRegionDecoder() != true) {
            logger.d(MODULE) {
                "MimeType does not support Subsampling. drawableSize: %dx%d, imageSize: %dx%d, mimeType: %s. '%s'"
                    .format(
                        drawableWidth,
                        drawableHeight,
                        imageWidth,
                        imageHeight,
                        mimeType,
                        requestKey
                    )
            }
            return null
        }

        logger.d(MODULE) {
            "Use Subsampling. drawableSize: %dx%d, imageSize: %dx%d, mimeType: %s. '%s'"
                .format(
                    drawableWidth,
                    drawableHeight,
                    imageWidth,
                    imageHeight,
                    mimeType,
                    requestKey
                )
        }

        val memoryCachePolicy: CachePolicy
        val disallowReuseBitmap: Boolean
        val displayResult = SketchUtils.getResult(imageView)
        if (displayResult != null && displayResult is DisplayResult.Success && displayResult.requestKey == requestKey) {
            memoryCachePolicy = displayResult.request.memoryCachePolicy
            disallowReuseBitmap = displayResult.request.disallowReuseBitmap
        } else {
            memoryCachePolicy = CachePolicy.ENABLED
            disallowReuseBitmap = false
        }
        return SubsamplingHelper(
            context = imageView.context,
            sketch = sketch,
            zoomAbility = zoomAbility,
            imageUri = sketchDrawable.imageUri,
            imageInfo = sketchDrawable.imageInfo,
            viewSize = viewContentSize,
            memoryCachePolicy = memoryCachePolicy,
            disallowReuseBitmap = disallowReuseBitmap,
        ).apply {
            this@apply.showTileBounds = this@SubsamplingAbility.showTileBounds
            this@apply.showTileBounds = this@SubsamplingAbility.showTileBounds
            this@apply.paused =
                this@SubsamplingAbility.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == false
            this@SubsamplingAbility.onTileChangedListenerList?.forEach {
                this@apply.addOnTileChangedListener(it)
            }
        }
    }

    fun eachTileList(action: (tile: Tile, load: Boolean) -> Unit) {
        subsamplingHelper?.eachTileList(action)
    }

    fun addOnTileChangedListener(listener: OnTileChangedListener) {
        this.onTileChangedListenerList = (onTileChangedListenerList ?: LinkedHashSet()).apply {
            add(listener)
        }
        subsamplingHelper?.addOnTileChangedListener(listener)
    }

    fun removeOnTileChangedListener(listener: OnTileChangedListener): Boolean {
        subsamplingHelper?.removeOnTileChangedListener(listener)
        return onTileChangedListenerList?.remove(listener) == true
    }
}