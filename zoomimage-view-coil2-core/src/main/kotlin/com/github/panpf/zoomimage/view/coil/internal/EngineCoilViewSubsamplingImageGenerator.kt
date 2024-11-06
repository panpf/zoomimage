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

package com.github.panpf.zoomimage.view.coil.internal

import android.content.Context
import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.request.SuccessResult
import com.github.panpf.zoomimage.coil.internal.dataToImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.coil.CoilViewSubsamplingImageGenerator

/**
 * [CoilViewSubsamplingImageGenerator] implementation that uses the Coil library to generate images
 *
 * @see com.github.panpf.zoomimage.view.coil2.core.test.internal.EngineCoilViewSubsamplingImageGeneratorTest
 */
class EngineCoilViewSubsamplingImageGenerator : CoilViewSubsamplingImageGenerator {

    override suspend fun generateImage(
        context: Context,
        imageLoader: ImageLoader,
        result: SuccessResult,
        drawable: Drawable
    ): SubsamplingImageGenerateResult {
        val data = result.request.data
        val imageSource = dataToImageSource(context, imageLoader, data)
            ?: return SubsamplingImageGenerateResult.Error("Unsupported data")
        return SubsamplingImageGenerateResult.Success(SubsamplingImage(imageSource, null))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other != null && this::class == other::class
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String {
        return "EngineCoilViewSubsamplingImageGenerator"
    }
}