package com.github.panpf.zoomimage.view.subsampling.internal

import android.view.View
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.view.zoom.OnContainerSizeChangeListener
import com.github.panpf.zoomimage.view.zoom.internal.ZoomEngine
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
    subsamplingEngine.containerSize = zoomEngine.containerSize
    zoomEngine.registerOnContainerSizeChangeListener(
        OnContainerSizeChangeListenerImpl(subsamplingEngine)
    )

    subsamplingEngine.contentSize = zoomEngine.contentSize
    zoomEngine.registerOnContentSizeChangeListener {
        subsamplingEngine.contentSize = zoomEngine.contentSize
    }

    zoomEngine.registerOnTransformChangeListener {
        val contentVisibleRect = zoomEngine.contentVisibleRect
        if (!zoomEngine.transforming && zoomEngine.transform.rotation.roundToInt() % 90 == 0) {
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