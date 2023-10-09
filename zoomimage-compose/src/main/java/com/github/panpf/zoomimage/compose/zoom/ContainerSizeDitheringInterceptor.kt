package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.Logger
import kotlin.math.abs

/*
 * In the model MIX4; ROM: 14.0.6.0; on Android 13, when the navigation bar is displayed, the following occurs:
 * 1. When the ZoomImageView is unlocked again after the screen is locked, the height of the ZoomImageView will first increase and then change back to normal, and the difference is exactly the height of the current navigation bar
 * 2. Due to the height of the ZoomImageView, the containerSize of the ZoomableEngine will also change
 * 3. This causes the ZoomableEngine's transform to be reset, so this needs to be blocked here
 */

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun rememberContainerSizeDitheringInterceptor(): ContainerSizeInterceptor {
    val density = LocalDensity.current
    val navigationBarHeightState = remember { NavigationBarHeightState() }
    val navigationBarsInsets = WindowInsets.navigationBarsIgnoringVisibility
    return remember {
        object : ContainerSizeInterceptor {
            override fun intercept(
                logger: Logger,
                oldContainerSize: IntSize,
                newContainerSize: IntSize
            ): IntSize {
                val newNavigationBarHeight = navigationBarsInsets.getBottom(density)
                if (newNavigationBarHeight != 0 && newNavigationBarHeight != navigationBarHeightState.navigationBarHeight) {
                    navigationBarHeightState.navigationBarHeight = newNavigationBarHeight
                }

                if (newContainerSize == oldContainerSize) return oldContainerSize

                val diffSize = IntSize(
                    width = newContainerSize.width - oldContainerSize.width,
                    height = newContainerSize.height - oldContainerSize.height
                )
                val navigationBarHeight = navigationBarHeightState.navigationBarHeight
                if (navigationBarHeight == 0 ||
                    (abs(diffSize.width) != navigationBarHeight && abs(diffSize.height) != navigationBarHeight)
                ) {
                    return newContainerSize
                }

                logger.d {
                    "onSizeChanged. intercepted. " +
                            "oldContainerSize=$oldContainerSize, " +
                            "newContainerSize=$newContainerSize, " +
                            "diffSize=$diffSize, " +
                            "navigationBarHeight=$navigationBarHeight"
                }
                return oldContainerSize
            }
        }
    }
}

private class NavigationBarHeightState(var navigationBarHeight: Int = 0)