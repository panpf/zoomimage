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

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.drawable.internal.CrossfadeDrawable
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.zoomimage.compose.sketch.SketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.google.accompanist.drawablepainter.DrawablePainter

/**
 * Filter animated images, animated images do not support subsampling
 *
 * @see com.github.panpf.zoomimage.compose.sketch3.core.test.internal.AnimatableSketchComposeSubsamplingImageGeneratorTest
 */
class AnimatableSketchComposeSubsamplingImageGenerator :
    SketchComposeSubsamplingImageGenerator {

    override suspend fun generateImage(
        sketch: Sketch,
        result: DisplayResult.Success,
        painter: Painter
    ): SubsamplingImageGenerateResult? {
        if (painter is DrawablePainter) {
            val drawable = painter.drawable
            val leafDrawable = drawable.findLeafChildDrawable()
            if (leafDrawable !is CrossfadeDrawable && leafDrawable is Animatable) {
                return SubsamplingImageGenerateResult.Error("Animated images do not support subsampling")
            }
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other != null && this::class == other::class
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun toString(): String {
        return "AnimatableSketchComposeSubsamplingImageGenerator"
    }

    /**
     * Find the last child [Drawable] from the specified Drawable
     */
    private fun Drawable.findLeafChildDrawable(): Drawable {
        return when (val drawable = this) {
            is CrossfadeDrawable -> {
                drawable.end?.findLeafChildDrawable() ?: drawable
            }

            is LayerDrawable -> {
                val layerCount = drawable.numberOfLayers
                if (layerCount > 0) {
                    drawable.getDrawable(layerCount - 1).findLeafChildDrawable()
                } else {
                    drawable
                }
            }

            else -> drawable
        }
    }
}