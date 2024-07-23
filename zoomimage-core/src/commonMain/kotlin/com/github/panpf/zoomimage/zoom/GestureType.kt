package com.github.panpf.zoomimage.zoom

import com.github.panpf.zoomimage.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    GestureType.DRAG,
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
        const val DRAG = 1

        const val TWO_FINGER_SCALE = 2

        const val ONE_FINGER_SCALE = 4

        const val DOUBLE_TAP_SCALE = 8

        fun name(@GestureType type: Int): String {
            return when (type) {
                DRAG -> "DRAG"
                TWO_FINGER_SCALE -> "TWO_FINGER_SCALE"
                ONE_FINGER_SCALE -> "ONE_FINGER_SCALE"
                DOUBLE_TAP_SCALE -> "DOUBLE_TAP_SCALE"
                else -> "UNKNOWN"
            }
        }

        val values = listOf(DRAG, TWO_FINGER_SCALE, ONE_FINGER_SCALE, DOUBLE_TAP_SCALE)

        fun parse(gestureTypes: Int): List<Int> {
            return values.asSequence().filter { gestureTypes and it != 0 }.toList()
        }
    }
}
