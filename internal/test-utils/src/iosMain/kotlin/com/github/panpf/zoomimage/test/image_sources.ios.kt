package com.github.panpf.zoomimage.test

import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource

actual fun ResourceImageFile.toImageSource(): ImageSource {
    return ImageSource.fromKotlinResource(resourceName)
}