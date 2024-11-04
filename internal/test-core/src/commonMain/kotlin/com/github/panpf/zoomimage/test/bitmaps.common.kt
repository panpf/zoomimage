package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.TileBitmap

expect fun createBitmap(width: Int, height: Int): TileBitmap

expect fun createA8Bitmap(width: Int, height: Int): TileBitmap

expect fun createRGB565Bitmap(width: Int, height: Int): TileBitmap

/**
 * Create thumbnails with specified width and height
 */
expect fun TileBitmap.thumbnail(width: Int, height: Int): TileBitmap

/**
 * Returns the Color at the specified location. Format ARGB_8888
 */
expect fun TileBitmap.readIntPixel(x: Int, y: Int): Int