package com.github.panpf.zoomimage.compose.zoom

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.PanToScaleTransformer
import com.github.panpf.zoomimage.zoom.VibrationHapticFeedback
import com.github.panpf.zoomimage.zoom.vibration

@Composable
@RequiresPermission("android.permission.VIBRATE")
fun rememberVibrationOneFingerScaleSpec(
    context: Context,
    milliseconds: Long = VibrationHapticFeedback.DefaultMilliseconds,
    @IntRange(from = -1, to = 255)
    amplitude: Int = VibrationHapticFeedback.DefaultAmplitude,
    panToScaleTransformer: PanToScaleTransformer = PanToScaleTransformer.Default
): OneFingerScaleSpec {
    return remember(context, milliseconds, amplitude, panToScaleTransformer) {
        OneFingerScaleSpec.vibration(context, milliseconds, amplitude, panToScaleTransformer)
    }
}