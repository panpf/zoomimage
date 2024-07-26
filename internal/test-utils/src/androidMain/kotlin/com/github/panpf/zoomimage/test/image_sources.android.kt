package com.github.panpf.zoomimage.test

import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromAsset

actual fun ResourceImageFile.toImageSource(): ImageSource {
    val context = InstrumentationRegistry.getInstrumentation().context
    return ImageSource.fromAsset(context, resourceName)
}