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

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View


/*
 * View GESTURE_END feedback
 */


fun OneFingerScaleSpec.Companion.viewGesture(
    view: View,
): OneFingerScaleSpec {
    val feedback = OneFingerScaleSpec.HapticFeedback.viewGesture(view)
    return OneFingerScaleSpec(feedback)
}

fun OneFingerScaleSpec.HapticFeedback.Companion.viewGesture(
    view: View,
): ViewGestureHapticFeedback {
    return ViewGestureHapticFeedback(view)
}

data class ViewGestureHapticFeedback(val view: View) : OneFingerScaleSpec.HapticFeedback {

    override fun perform() {
        val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.GESTURE_END
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            HapticFeedbackConstants.CONTEXT_CLICK
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(feedbackConstant)
    }
}