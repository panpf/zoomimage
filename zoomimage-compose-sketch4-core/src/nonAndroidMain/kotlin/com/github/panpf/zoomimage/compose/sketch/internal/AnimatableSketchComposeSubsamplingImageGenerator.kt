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

package com.github.panpf.zoomimage.compose.sketch.internal

import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.painter.AnimatablePainter
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.util.findLeafPainter
import com.github.panpf.zoomimage.compose.sketch.SketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult

/**
 * Filter animated images, animated images do not support subsampling
 *
 * @see com.github.panpf.zoomimage.compose.sketch4.core.nonandroid.test.internal.AnimatableSketchComposeSubsamplingImageGeneratorTest
 */
actual data object AnimatableSketchComposeSubsamplingImageGenerator :
    SketchComposeSubsamplingImageGenerator {

    actual override suspend fun generateImage(
        sketch: Sketch,
        request: ImageRequest,
        result: ImageResult.Success,
        painter: Painter
    ): SubsamplingImageGenerateResult? {
        val leafPainter = painter.findLeafPainter()
        if (leafPainter is AnimatablePainter) {
            return SubsamplingImageGenerateResult.Error("Animated images do not support subsampling")
        }
        return null
    }
}