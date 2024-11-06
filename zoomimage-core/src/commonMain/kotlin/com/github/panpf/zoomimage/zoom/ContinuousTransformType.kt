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

package com.github.panpf.zoomimage.zoom

import androidx.annotation.IntDef
import com.github.panpf.zoomimage.zoom.ContinuousTransformType.Companion.FLING
import com.github.panpf.zoomimage.zoom.ContinuousTransformType.Companion.GESTURE
import com.github.panpf.zoomimage.zoom.ContinuousTransformType.Companion.LOCATE
import com.github.panpf.zoomimage.zoom.ContinuousTransformType.Companion.OFFSET
import com.github.panpf.zoomimage.zoom.ContinuousTransformType.Companion.SCALE

/**
 * Continuous transformation type
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.ContinuousTransformTypeTest
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(SCALE, OFFSET, LOCATE, GESTURE, FLING)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class ContinuousTransformType {
    companion object {

        /**
         * scale(), switchScale(), rollbackScale() functions
         */
        const val SCALE = 1

        /**
         * offset() functions
         */
        const val OFFSET = 2

        /**
         * locate() functions
         */
        const val LOCATE = 4

        /**
         * User gestures dragging and zooming
         */
        const val GESTURE = 8

        /**
         * User gesture fling
         */
        const val FLING = 16

        val values = listOf(SCALE, OFFSET, LOCATE, GESTURE, FLING)

        fun name(@ContinuousTransformType type: Int): String = when (type) {
            SCALE -> "SCALE"
            OFFSET -> "OFFSET"
            LOCATE -> "LOCATE"
            GESTURE -> "GESTURE"
            FLING -> "FLING"
            else -> "UNKNOWN"
        }

        fun parse(continuousTransformTypes: Int): List<Int> {
            return values.asSequence().filter { continuousTransformTypes and it != 0 }.toList()
        }
    }
}