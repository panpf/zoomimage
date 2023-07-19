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
        if (!zoomEngine.isScaling && zoomEngine.rotateDegrees % 90 == 0) {
            zoomEngine.getVisibleRect(contentVisibleRect)
            subsamplingEngine.refreshTiles(
                displayScale = zoomEngine.displayScale.scaleX,
                displayMinScale = zoomEngine.minUserScale * zoomEngine.baseScale.scaleX,
                contentVisibleRect = contentVisibleRect,
                caller = "matrixChanged"
            )
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