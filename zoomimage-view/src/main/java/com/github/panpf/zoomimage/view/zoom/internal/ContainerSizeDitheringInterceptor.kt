package com.github.panpf.zoomimage.view.zoom.internal

import android.view.View
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.view.internal.getNavigationBarsHeight
import com.github.panpf.zoomimage.view.zoom.ContainerSizeInterceptor
import kotlin.math.abs

/*
 * In the model MIX4; ROM: 14.0.6.0; on Android 13, when the navigation bar is displayed, the following occurs:
 * 1. When the ZoomImageView is unlocked again after the screen is locked, the height of the ZoomImageView will first increase and then change back to normal, and the difference is exactly the height of the current navigation bar
 * 2. Due to the height of the ZoomImageView, the containerSize of the ZoomableEngine will also change
 * 3. This causes the ZoomableEngine's transform to be reset, so this needs to be blocked here
 */
class ContainerSizeDitheringInterceptor(val view: View) : ContainerSizeInterceptor {

    private var navigationBarsHeight = 0

    override fun intercept(
        logger: Logger,
        oldContainerSize: IntSizeCompat,
        newContainerSize: IntSizeCompat
    ): IntSizeCompat {
        updateNavigationBarsHeight()

        if (newContainerSize == oldContainerSize) return oldContainerSize

        val diffSize = IntSizeCompat(
            width = oldContainerSize.width - newContainerSize.width,
            height = oldContainerSize.height - newContainerSize.height
        )
        val navigationBarHeight = navigationBarsHeight
        if (navigationBarHeight == 0 ||
            (abs(diffSize.width) != navigationBarHeight && abs(diffSize.height) != navigationBarHeight)
        ) {
            return newContainerSize
        }

        logger.d {
            "updateContainerSize. intercepted. " +
                    "oldContainerSize=$oldContainerSize, " +
                    "newContainerSize=$newContainerSize, " +
                    "diffSize=$diffSize, " +
                    "navigationBarHeight=$navigationBarHeight"
        }
        return oldContainerSize
    }

    private fun updateNavigationBarsHeight() {
        val newNavigationBarsHeight = view.getNavigationBarsHeight()
        if (newNavigationBarsHeight != 0 && newNavigationBarsHeight != navigationBarsHeight) {
            navigationBarsHeight = newNavigationBarsHeight
        }
    }

    private fun updateContainerSize() {

    }
}