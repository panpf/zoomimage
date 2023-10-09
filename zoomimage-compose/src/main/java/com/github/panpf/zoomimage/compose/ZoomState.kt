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

package com.github.panpf.zoomimage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.zoomimage.AndroidLogPipeline
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberContainerSizeDitheringInterceptor
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.subsampling.AndroidTilePlatformAdapter

/**
 * Creates and remember a [ZoomState]
 */
@Composable
fun rememberZoomState(): ZoomState {
    val logPipeline = remember { AndroidLogPipeline() }
    val logger = rememberZoomImageLogger(pipeline = logPipeline)

    val zoomableState = rememberZoomableState(logger)
    zoomableState.containerSizeInterceptor = rememberContainerSizeDitheringInterceptor()

    val tilePlatformAdapter = remember { AndroidTilePlatformAdapter() }
    val subsamplingState = rememberSubsamplingState(logger, tilePlatformAdapter)
    subsamplingState.BindZoomableState(zoomableState)

    val zoomState = remember(logger, zoomableState, subsamplingState) {
        ZoomState(logger, zoomableState, subsamplingState)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        zoomState.setLifecycle(lifecycle)
    }
    return zoomState
}

/**
 * Used to control the state of scaling, translation, rotation, and subsampling
 */
@Stable
class ZoomState(
    /**
     * Used to print log
     */
    val logger: Logger,

    /**
     * Used to control the state of scaling, translation, and rotation
     */
    val zoomable: ZoomableState,

    /**
     * Used to control the state of subsampling
     */
    val subsampling: SubsamplingState,
) : RememberObserver {

    private var lifecycle: Lifecycle? = null
    private val resetStoppedLifecycleObserver by lazy { ResetStoppedLifecycleObserver(this) }

    /**
     * Set the lifecycle, which automatically controls stop and start, which is obtained from [LocalLifecycleOwner] by default,
     * and can be set by this method if the default acquisition method is not applicable
     */
    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetStopped("setLifecycle")
        }
    }

    private fun resetStopped(caller: String) {
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val stopped = !lifecycleStarted
        logger.d {
            "resetStopped:$caller. $stopped. lifecycleStarted=$lifecycleStarted. '${subsampling.imageKey}'"
        }
        subsampling.stopped = stopped
    }

    private fun registerLifecycleObserver() {
        lifecycle?.addObserver(resetStoppedLifecycleObserver)
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(resetStoppedLifecycleObserver)
    }

    private class ResetStoppedLifecycleObserver(val state: ZoomState) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_START) {
                state.resetStopped("LifecycleStateChanged:ON_START")
            } else if (event == Lifecycle.Event.ON_STOP) {
                state.resetStopped("LifecycleStateChanged:ON_STOP")
            }
        }
    }

    override fun toString(): String {
        return "ZoomState(logger=${logger}, zoomable=${zoomable}, subsampling=${subsampling})"
    }

    override fun onRemembered() {

    }

    override fun onAbandoned() {
        unregisterLifecycleObserver()
    }

    override fun onForgotten() {
        unregisterLifecycleObserver()
    }
}