/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.zoom.internal

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContainerSizeInterceptor
import kotlin.math.abs

/*
 * In the model MIX4; ROM: 14.0.6.0; on Android 13, when the navigation bar is displayed, the following occurs:
 * 1. When the ZoomImageView is unlocked again after the screen is locked, the height of the ZoomImageView will first increase and then change back to normal, and the difference is exactly the height of the current navigation bar
 * 2. Due to the height of the ZoomImageView, the containerSize of the ZoomableEngine will also change
 * 3. This causes the ZoomableEngine's transform to be reset, so this needs to be blocked here
 */
class NavigationBarDitherContainerSizeInterceptor(
    private val navigationBarHeightGetter: NavigationBarHeightGetter
) : ContainerSizeInterceptor {

    private var navigationBarHeight: Int = 0

    override fun intercept(
        logger: Logger,
        oldContainerSize: IntSizeCompat,
        newContainerSize: IntSizeCompat
    ): IntSizeCompat {
        val newNavigationBarHeight = navigationBarHeightGetter.getNavigationBarHeight()
        if (newNavigationBarHeight != 0 && newNavigationBarHeight != navigationBarHeight) {
            navigationBarHeight = newNavigationBarHeight
        }

        if (newContainerSize == oldContainerSize) return oldContainerSize

        val diffSize = IntSizeCompat(
            width = newContainerSize.width - oldContainerSize.width,
            height = newContainerSize.height - oldContainerSize.height
        )
        val navigationBarHeight = navigationBarHeight
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

    interface NavigationBarHeightGetter {
        fun getNavigationBarHeight(): Int
    }
}