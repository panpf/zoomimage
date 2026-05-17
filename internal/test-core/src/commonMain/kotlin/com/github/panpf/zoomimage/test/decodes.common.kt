package com.github.panpf.zoomimage.test

import com.githb.panpf.zoomimage.images.ComposeResImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap

expect suspend fun ComposeResImageFile.decode(): TileBitmap