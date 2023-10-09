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


/*
 * Vibration feedback
 */


fun LongPressSlideScaleSpec.Companion.vibration(
    context: Context,
    milliseconds: Long = VibrationHapticFeedback.DefaultMilliseconds,
    @IntRange(from = -1, to = 255)
    amplitude: Int = VibrationHapticFeedback.DefaultAmplitude
): LongPressSlideScaleSpec {
    val feedback =
        LongPressSlideScaleSpec.HapticFeedback.vibration(context, milliseconds, amplitude)
    return LongPressSlideScaleSpec(feedback)
}


fun LongPressSlideScaleSpec.HapticFeedback.Companion.vibration(
    context: Context,
    milliseconds: Long = VibrationHapticFeedback.DefaultMilliseconds,
    @IntRange(from = -1, to = 255)
    amplitude: Int = VibrationHapticFeedback.DefaultAmplitude
): VibrationHapticFeedback {
    return VibrationHapticFeedback(context, milliseconds, amplitude)
}

data class VibrationHapticFeedback(
    val context: Context,
    val milliseconds: Long = DefaultMilliseconds,
    @IntRange(from = -1, to = 255) val amplitude: Int = DefaultAmplitude
) : LongPressSlideScaleSpec.HapticFeedback {

    companion object {
        const val DefaultMilliseconds = 50L
        const val DefaultAmplitude = -1
    }

    override fun perform() {
        @Suppress("DEPRECATION") val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService(Vibrator::class.java)
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect =
                VibrationEffect.createOneShot(milliseconds, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }
}