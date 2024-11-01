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

package com.github.panpf.zoomimage.view.sketch.internal

import android.graphics.drawable.Drawable
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.sketch.SketchViewSubsamplingImageGenerator

/**
 * [SketchViewSubsamplingImageGenerator] implementation that uses the Sketch library to generate images
 *
 * @see com.github.panpf.zoomimage.view.sketch4.core.test.internal.EngineSketchViewSubsamplingImageGeneratorTest
 */
data object EngineSketchViewSubsamplingImageGenerator : SketchViewSubsamplingImageGenerator {

    override suspend fun generateImage(
        sketch: Sketch,
        request: ImageRequest,
        result: ImageResult.Success,
        drawable: Drawable
    ): SubsamplingImageGenerateResult {
        val imageSource = SketchImageSource.Factory(sketch, request.uri.toString())
        val imageInfo = ImageInfo(
            width = result.imageInfo.width,
            height = result.imageInfo.height,
            mimeType = result.imageInfo.mimeType
        )
        return SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource, imageInfo))
    }
}