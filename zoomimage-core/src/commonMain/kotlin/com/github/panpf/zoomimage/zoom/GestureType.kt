package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.annotation.IntDef

/**
 * Gesture type
 *
 * @see com.github.panpf.zoomimage.core.common.test.zoom.GestureTypeTest
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    GestureType.ONE_FINGER_DRAG,
    GestureType.TWO_FINGER_SCALE,
    GestureType.ONE_FINGER_SCALE,
    GestureType.DOUBLE_TAP_SCALE,
)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class GestureType {

    companion object {
        const val ONE_FINGER_DRAG = 1

        const val TWO_FINGER_SCALE = 2

        const val ONE_FINGER_SCALE = 4

        const val DOUBLE_TAP_SCALE = 8

        const val MOUSE_WHEEL_SCALE = 16

        const val KEYBOARD_SCALE = 32

        const val KEYBOARD_DRAG = 64

        val values = listOf(
            ONE_FINGER_DRAG,
            TWO_FINGER_SCALE,
            ONE_FINGER_SCALE,
            DOUBLE_TAP_SCALE,
            MOUSE_WHEEL_SCALE,
            KEYBOARD_SCALE,
            KEYBOARD_DRAG
        )

        fun name(@GestureType type: Int): String = when (type) {
            ONE_FINGER_DRAG -> "ONE_FINGER_DRAG"
            TWO_FINGER_SCALE -> "TWO_FINGER_SCALE"
            ONE_FINGER_SCALE -> "ONE_FINGER_SCALE"
            DOUBLE_TAP_SCALE -> "DOUBLE_TAP_SCALE"
            MOUSE_WHEEL_SCALE -> "MOUSE_WHEEL_SCALE"
            KEYBOARD_SCALE -> "KEYBOARD_SCALE"
            KEYBOARD_DRAG -> "KEYBOARD_DRAG"
            else -> "UNKNOWN"
        }

        fun parse(gestureTypes: Int): List<Int> {
            return values.asSequence().filter { gestureTypes and it != 0 }.toList()
        }
    }
}
