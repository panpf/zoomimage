package com.github.panpf.zoomimage.test

import android.content.Context
import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset

fun ResourceImageFile.toImageSource(context: Context): ImageSource {
    return ImageSource.fromAsset(context, resourceName)
}