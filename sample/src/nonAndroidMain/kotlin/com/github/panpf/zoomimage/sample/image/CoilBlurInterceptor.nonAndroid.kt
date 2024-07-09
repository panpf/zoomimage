package com.github.panpf.zoomimage.sample.image

import coil3.BitmapImage
import coil3.asImage
import com.github.panpf.sketch.SkiaBitmapImage
import com.github.panpf.sketch.asSketchImage


actual fun BitmapImage.asSketchImage(): com.github.panpf.sketch.Image {
    return bitmap.asSketchImage()
}

actual fun com.github.panpf.sketch.Image.asCoilBitmapImage(): BitmapImage {
    val bitmap = (this as SkiaBitmapImage).bitmap
    return bitmap.asImage()
}