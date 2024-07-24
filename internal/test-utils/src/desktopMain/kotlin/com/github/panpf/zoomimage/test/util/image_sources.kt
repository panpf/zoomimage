package com.github.panpf.zoomimage.test.util

import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromKotlinResource

fun ResourceImageFile.toImageSource(): ImageSource {
    return ImageSource.fromKotlinResource(resourceName)
}