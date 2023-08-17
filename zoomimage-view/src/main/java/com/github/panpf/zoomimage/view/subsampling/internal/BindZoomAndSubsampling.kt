package com.github.panpf.zoomimage.view.subsampling.internal

import android.graphics.Rect
import android.view.View
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.view.zoom.OnViewSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

fun bindZoomAndSubsampling(
    view: View,
    zoomEngine: ZoomEngine,
    subsamplingEngine: SubsamplingEngine
) {
    subsamplingEngine.containerSize = zoomEngine.viewSize
    zoomEngine.addOnViewSizeChangeListener(
        OnViewSizeChangeListenerImpl(zoomEngine, subsamplingEngine)
    )

    subsamplingEngine.contentSize = zoomEngine.drawableSize
    zoomEngine.addOnDrawableSizeChangeListener {
        subsamplingEngine.contentSize = zoomEngine.drawableSize
    }

    val contentVisibleRect = Rect()
    zoomEngine.addOnMatrixChangeListener {
        zoomEngine.getVisibleRect(contentVisibleRect)
        if (!zoomEngine.scaling && !zoomEngine.fling && zoomEngine.rotation % 90 == 0) {
            subsamplingEngine.refreshTiles(
                displayScale = zoomEngine.scale.scaleX,
                displayMinScale = zoomEngine.minScale,
                contentVisibleRect = contentVisibleRect,
                caller = "matrixChanged"
            )
        } else {
            subsamplingEngine.resetVisibleAndLoadRect(contentVisibleRect, "matrixChanged")
        }
    }

    subsamplingEngine.addOnReadyChangeListener {
        val imageInfo = subsamplingEngine.imageInfo
        zoomEngine.imageSize = if (subsamplingEngine.ready && imageInfo != null) {
            imageInfo.size
        } else {
            IntSizeCompat.Zero
        }
    }

    subsamplingEngine.addOnTileChangedListener {
        view.invalidate()
    }
}

fun bindZoomAndSubsampling2(
    view: View,
    zoomEngine: ZoomEngine2,
    subsamplingEngine: SubsamplingEngine
) {
    subsamplingEngine.containerSize = zoomEngine.containerSize
    zoomEngine.addOnViewSizeChangeListener(
        OnViewSizeChangeListenerImpl2(zoomEngine, subsamplingEngine)
    )

    subsamplingEngine.contentSize = zoomEngine.contentSize
    zoomEngine.addOnDrawableSizeChangeListener {
        subsamplingEngine.contentSize = zoomEngine.contentSize
    }

    zoomEngine.addOnMatrixChangeListener {
        val contentVisibleRect = zoomEngine.contentVisibleRect.let { Rect(it.left, it.top, it.right, it.bottom) }
        if (!zoomEngine.scaling && !zoomEngine.fling && zoomEngine.transform.rotation.roundToInt() % 90 == 0) {
            subsamplingEngine.refreshTiles(
                displayScale = zoomEngine.transform.scaleX,
                displayMinScale = zoomEngine.minScale,
                contentVisibleRect = contentVisibleRect,
                caller = "matrixChanged"
            )
        } else {
            subsamplingEngine.resetVisibleAndLoadRect(contentVisibleRect, "matrixChanged")
        }
    }

    subsamplingEngine.addOnReadyChangeListener {
        val imageInfo = subsamplingEngine.imageInfo
        zoomEngine.contentOriginSize = if (subsamplingEngine.ready && imageInfo != null) {
            imageInfo.size
        } else {
            IntSizeCompat.Zero
        }
    }

    subsamplingEngine.addOnTileChangedListener {
        view.invalidate()
    }
}

private class OnViewSizeChangeListenerImpl(
    private val zoomEngine: ZoomEngine,
    private val subsamplingEngine: SubsamplingEngine
) : OnViewSizeChangeListener {

    private var lastDelayJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    override fun onSizeChanged() {
        // Changes in viewSize cause a large chain reaction that can cause large memory fluctuations.
        // View size animations cause frequent changes in viewSize, so a delayed reset avoids this problem
        lastDelayJob?.cancel()
        lastDelayJob = scope.launch(Dispatchers.Main) {
            delay(60)
            lastDelayJob = null
            subsamplingEngine.containerSize = zoomEngine.viewSize
        }
    }
}

private class OnViewSizeChangeListenerImpl2(
    private val zoomEngine: ZoomEngine2,
    private val subsamplingEngine: SubsamplingEngine
) : OnViewSizeChangeListener {

    private var lastDelayJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    override fun onSizeChanged() {
        // Changes in viewSize cause a large chain reaction that can cause large memory fluctuations.
        // View size animations cause frequent changes in viewSize, so a delayed reset avoids this problem
        lastDelayJob?.cancel()
        lastDelayJob = scope.launch(Dispatchers.Main) {
            delay(60)
            lastDelayJob = null
            subsamplingEngine.containerSize = zoomEngine.containerSize
        }
    }
}