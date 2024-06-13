package com.github.panpf.zoomimage.coil

import coil3.ImageLoader
import com.github.panpf.zoomimage.subsampling.TileBitmapCache

expect class CoilTileBitmapCache(imageLoader: ImageLoader) : TileBitmapCache