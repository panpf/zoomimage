package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toRect
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect

@Composable
@OptIn(ExperimentalComposeUiApi::class)
actual fun windowSize(): IntSize {
    val windowInfo = LocalWindowInfo.current
    return windowInfo.containerSize
}

actual fun ImageBitmap.crop(rect: IntRect): ImageBitmap {
    return this.asSkiaBitmap().crop(rect).asComposeImageBitmap()
}

fun Bitmap.crop(rect: IntRect): Bitmap {
    require(!rect.isEmpty) { "Rect must not be empty. rect=$rect" }
    require(
        rect.left >= 0
                && rect.top >= 0
                && rect.right <= this@crop.width
                && rect.bottom <= this@crop.height
    ) {
        "Rect must be within the bounds of the image. imageSize=${this@crop.width}x${this@crop.height}, rect=$rect"
    }
    val skiaBitmap = this
    val croppedBitmap = Bitmap().apply {
        allocN32Pixels(width = rect.width, height = rect.height)
    }
    val canvas = Canvas(croppedBitmap)
    canvas.drawImageRect(
        image = Image.makeFromBitmap(skiaBitmap),
        src = rect.toRect().toSkiaRect(),
        dst = Rect.makeWH(w = croppedBitmap.width.toFloat(), h = croppedBitmap.height.toFloat())
    )
    return croppedBitmap
}