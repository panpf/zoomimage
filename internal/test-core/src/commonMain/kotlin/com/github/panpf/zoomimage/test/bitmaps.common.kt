package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.TileBitmap

expect fun createBitmap(width: Int, height: Int): TileBitmap

expect fun createA8Bitmap(width: Int, height: Int): TileBitmap

expect fun createRGB565Bitmap(width: Int, height: Int): TileBitmap