package com.github.panpf.zoomimage.sample.image

import coil3.BitmapImage
import coil3.asImage
import com.github.panpf.sketch.AndroidBitmapImage
import com.github.panpf.sketch.asSketchImage


actual fun BitmapImage.asSketchImage(): com.github.panpf.sketch.Image {
    return bitmap.asSketchImage()
}

actual fun com.github.panpf.sketch.Image.asCoilBitmapImage(): BitmapImage {
    val bitmap = (this as AndroidBitmapImage).bitmap
    return bitmap.asImage()
}