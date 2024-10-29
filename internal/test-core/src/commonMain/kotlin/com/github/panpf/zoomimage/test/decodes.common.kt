package com.github.panpf.zoomimage.test

import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap

expect fun ResourceImageFile.decode(): TileBitmap