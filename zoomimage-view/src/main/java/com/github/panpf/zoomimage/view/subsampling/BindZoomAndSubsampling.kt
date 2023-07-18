package com.github.panpf.zoomimage.view.subsampling

import android.graphics.Rect
import android.view.View
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.view.zoom.OnViewSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun bindZoomAndSubsampling(
    view: View,
    zoomEngine: ZoomEngine,
    subsamplingEngine: SubsamplingEngine
) {
    subsamplingEngine.viewSize = zoomEngine.viewSize
    zoomEngine.addOnViewSizeChangeListener(
        OnViewSizeChangeListenerImpl(zoomEngine, subsamplingEngine)
    )

    subsamplingEngine.drawableSize = zoomEngine.drawableSize
    zoomEngine.addOnDrawableSizeChangeListener() {
        subsamplingEngine.drawableSize = zoomEngine.drawableSize
    }

    val drawableVisibleRect = Rect()
    zoomEngine.addOnMatrixChangeListener {
        zoomEngine.getVisibleRect(drawableVisibleRect)
        subsamplingEngine.refreshTiles(
            displayScale = zoomEngine.displayScale,
            rotation = zoomEngine.rotateDegrees,
            scaling = zoomEngine.isScaling,
            drawableVisibleRect = drawableVisibleRect
        )
    }

    subsamplingEngine.addOnReadyChangeListener {
        zoomEngine.imageSize = if (subsamplingEngine.ready) {
            subsamplingEngine.imageInfo?.size ?: IntSizeCompat.Zero
        } else {
            IntSizeCompat.Zero
        }
    }

    subsamplingEngine.addOnTileChangedListener() {
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
            subsamplingEngine.viewSize = zoomEngine.viewSize
        }
    }
}