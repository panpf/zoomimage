package com.github.panpf.zoomimage.test

import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.ImageSource

actual fun ResourceImageFile.toImageSource(): ImageSource {
    throw UnsupportedOperationException("No implementation for js platform")
}