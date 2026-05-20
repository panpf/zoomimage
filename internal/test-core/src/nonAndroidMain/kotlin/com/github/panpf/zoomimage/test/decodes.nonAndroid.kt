package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.images.ComposeResImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap
import okio.buffer
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect

actual suspend fun ComposeResImageFile.decode(): TileBitmap {
    val byteArray = toImageSource().openSource().buffer().use {
        it.readByteArray()
    }
    val image = Image.makeFromEncoded(byteArray)
    val bitmap = Bitmap().apply {
        allocN32Pixels(image.width, image.height)
    }
    val canvas = Canvas(bitmap)
    canvas.drawImageRect(
        image = image,
        src = Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
        dst = Rect.makeWH(bitmap.width.toFloat(), bitmap.height.toFloat())
    )
    return bitmap
}