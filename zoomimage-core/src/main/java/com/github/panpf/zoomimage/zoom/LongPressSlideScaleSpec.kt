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

package com.github.panpf.zoomimage.zoom

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.IntRange

/**
 * Long press and slide up and down to scale the configuration of the image
 */
data class LongPressSlideScaleSpec(
    val hapticFeedback: HapticFeedback = HapticFeedback.None,
    val panToScaleTransformer: PanToScaleTransformer = PanToScaleTransformer.Default
) {

    companion object {
        val Default = LongPressSlideScaleSpec()
    }

    interface HapticFeedback {

        companion object {

            val None = object : HapticFeedback {
                override fun perform(context: Context) {

                }
            }

            val Vibration = VibrationHapticFeedback()

            fun vibration(
                milliseconds: Long = VibrationHapticFeedback.DefaultMilliseconds,
                @IntRange(from = -1, to = 255)
                amplitude: Int = VibrationHapticFeedback.DefaultAmplitude
            ): VibrationHapticFeedback {
                return VibrationHapticFeedback(milliseconds, amplitude)
            }
        }

        fun perform(context: Context)
    }

    data class VibrationHapticFeedback(
        val milliseconds: Long = DefaultMilliseconds,
        @IntRange(from = -1, to = 255) val amplitude: Int = DefaultAmplitude
    ) : HapticFeedback {

        companion object {
            const val DefaultMilliseconds = 50L
            const val DefaultAmplitude = -1
        }

        override fun perform(context: Context) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(Vibrator::class.java)
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect =
                    VibrationEffect.createOneShot(milliseconds, amplitude)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(milliseconds)
            }
        }
    }

    interface PanToScaleTransformer {

        companion object {
            val Default = DefaultPanToScaleTransformer()

            fun default(reference: Int = DefaultPanToScaleTransformer.DefaultReference): DefaultPanToScaleTransformer {
                return DefaultPanToScaleTransformer(reference)
            }
        }

        fun transform(panY: Float): Float
    }

    class DefaultPanToScaleTransformer(val reference: Int = DefaultReference) :
        PanToScaleTransformer {

        companion object {
            const val DefaultReference: Int = 200
        }

        override fun transform(panY: Float): Float {
            return 1f + (panY / reference)
        }
    }
}