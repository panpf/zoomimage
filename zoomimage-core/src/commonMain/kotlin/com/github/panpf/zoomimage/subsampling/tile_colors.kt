package com.github.panpf.zoomimage.subsampling


private const val TILE_COLOR_RED: Int = 0xFFFF0000.toInt()
private const val TILE_COLOR_GREEN: Int = 0xFF00FF00.toInt()
private const val TILE_COLOR_YELLOW_GREEN: Int = 0xFF9ACD32.toInt()
private const val TILE_COLOR_BLUE: Int = 0xFF0000FF.toInt()
private const val TILE_COLOR_YELLOW: Int = 0xFFFFFF00.toInt()
private const val TILE_COLOR_CYAN: Int = 0xFF00FFFF.toInt()
private const val TILE_COLOR_MAGENTA: Int = 0xFFFF00FF.toInt()
private const val TILE_COLOR_SKY_BLUE: Int = 0xFF00CCFF.toInt()

fun tileColor(
    @TileState state: Int,
    bitmapFrom: BitmapFrom?,
    withinLoadArea: Boolean? = null
): Int = when {
    withinLoadArea == false -> TILE_COLOR_SKY_BLUE
    state == TileState.STATE_LOADED -> {
        when (bitmapFrom) {
            BitmapFrom.MEMORY_CACHE -> TILE_COLOR_GREEN
            BitmapFrom.LOCAL -> TILE_COLOR_YELLOW_GREEN
            else -> TILE_COLOR_GREEN
        }
    }

    state == TileState.STATE_LOADING -> TILE_COLOR_CYAN
    else -> TILE_COLOR_RED
}