/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.coil.CoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.coil.internal.AnimatableCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.coil.internal.EngineCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.Logger.Level
import kotlinx.collections.immutable.ImmutableList

/**
 * Creates and remember a [CoilZoomState]
 *
 * @see com.github.panpf.zoomimage.compose.coil2.core.test.CoilZoomStateTest.testRememberCoilZoomState
 */
@Composable
fun rememberCoilZoomState(
    subsamplingImageGenerators: ImmutableList<CoilComposeSubsamplingImageGenerator>? = null,
    logLevel: Level? = null,
): CoilZoomState {
    val logger: Logger = rememberZoomImageLogger(tag = "CoilZoomAsyncImage", level = logLevel)
    val zoomableState = rememberZoomableState(logger)
    val subsamplingState = rememberSubsamplingState(zoomableState)
    return remember(logger, zoomableState, subsamplingState, subsamplingImageGenerators) {
        CoilZoomState(logger, zoomableState, subsamplingState, subsamplingImageGenerators)
    }
}

/**
 * A [ZoomState] implementation that supports Coil
 *
 * @see com.github.panpf.zoomimage.compose.coil2.core.test.CoilZoomStateTest
 */
@Stable
class CoilZoomState(
    logger: Logger,
    zoomable: ZoomableState,
    subsampling: SubsamplingState,
    subsamplingImageGenerators: ImmutableList<CoilComposeSubsamplingImageGenerator>?
) : ZoomState(logger, zoomable, subsampling) {

    val subsamplingImageGenerators: List<CoilComposeSubsamplingImageGenerator> =
        subsamplingImageGenerators.orEmpty()
            .plus(
                listOf(
                    AnimatableCoilComposeSubsamplingImageGenerator(),
                    EngineCoilComposeSubsamplingImageGenerator()
                )
            )
}