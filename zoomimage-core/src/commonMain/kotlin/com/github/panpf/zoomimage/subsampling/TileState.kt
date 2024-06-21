package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    TileState.STATE_NONE,
    TileState.STATE_LOADING,
    TileState.STATE_LOADED,
    TileState.STATE_ERROR
)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class TileState {

    companion object {

        const val STATE_NONE = 0
        const val STATE_LOADING = 1
        const val STATE_LOADED = 2
        const val STATE_ERROR = 3

        fun name(state: Int): String = when (state) {
            STATE_NONE -> "NONE"
            STATE_LOADING -> "LOADING"
            STATE_LOADED -> "LOADED"
            STATE_ERROR -> "ERROR"
            else -> "UNKNOWN"
        }
    }
}