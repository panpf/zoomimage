package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.images.ComposeResImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap

expect suspend fun ComposeResImageFile.decode(): TileBitmap