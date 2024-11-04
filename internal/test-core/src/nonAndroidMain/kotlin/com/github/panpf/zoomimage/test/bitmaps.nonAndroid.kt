package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.TileBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.impl.use

actual fun createBitmap(width: Int, height: Int): TileBitmap {
    return TileBitmap().apply { allocN32Pixels(width, height) }
}

actual fun createA8Bitmap(width: Int, height: Int): TileBitmap {
    return TileBitmap().apply {
        allocPixels(
            ImageInfo(
                width,
                height,
                ColorType.ALPHA_8,
                alphaType,
                colorSpace
            )
        )
    }
}

actual fun createRGB565Bitmap(width: Int, height: Int): TileBitmap {
    return TileBitmap().apply {
        allocPixels(
            ImageInfo(
                width,
                height,
                ColorType.RGB_565,
                alphaType,
                colorSpace
            )
        )
    }
}

fun createBitmap(
    width: Int,
    height: Int,
    colorType: ColorType = ColorType.N32,
    alphaType: ColorAlphaType = ColorAlphaType.PREMUL,
    colorSpace: ColorSpace = ColorSpace.sRGB,
): Bitmap = Bitmap()
    .apply { allocPixels(ImageInfo(width, height, colorType, alphaType, colorSpace)) }

/**
 * Create a new [Bitmap] with the specified [ImageInfo] and allocate memory
 */
fun createBitmap(imageInfo: ImageInfo): Bitmap = Bitmap()
    .apply { allocPixels(imageInfo) }

/**
 * Create thumbnails with specified width and height
 */
actual fun Bitmap.thumbnail(width: Int, height: Int): Bitmap {
    val inputBitmap = this
    val outputBitmap = createBitmap(inputBitmap.imageInfo.withWidthHeight(width, height))
    val canvas = Canvas(outputBitmap)
    Image.makeFromBitmap(inputBitmap).use { skiaImage ->
        canvas.drawImageRect(
            image = skiaImage,
            src = org.jetbrains.skia.Rect(
                0f,
                0f,
                inputBitmap.width.toFloat(),
                inputBitmap.height.toFloat()
            ),
            dst = org.jetbrains.skia.Rect(
                0f,
                0f,
                outputBitmap.width.toFloat(),
                outputBitmap.height.toFloat()
            ),
        )
    }
    return outputBitmap
}


/**
 * Read an integer pixel array in the format ARGB_8888
 */
fun Bitmap.readIntPixels(
    x: Int, y: Int, width: Int, height: Int
): IntArray {
    val inputBitmap = this
    val rgbaBitmap = if (inputBitmap.colorType == ColorType.RGBA_8888) {
        inputBitmap
    } else {
        createBitmap(inputBitmap.imageInfo.withColorType(ColorType.RGBA_8888)).also { rgbaBitmap ->
            Image.makeFromBitmap(inputBitmap).use { inputImage ->
                Canvas(rgbaBitmap).drawImageRect(
                    image = inputImage,
                    src = org.jetbrains.skia.Rect.makeWH(
                        w = inputBitmap.width.toFloat(),
                        h = inputBitmap.height.toFloat()
                    ),
                    dst = org.jetbrains.skia.Rect.makeWH(
                        w = rgbaBitmap.width.toFloat(),
                        h = rgbaBitmap.height.toFloat()
                    ),
                    paint = Paint().apply {
                        isAntiAlias = true
                    },
                )
            }
        }
    }

    val imageInfo = ImageInfo(rgbaBitmap.colorInfo, width, height)
    val dstRowBytes = width * rgbaBitmap.bytesPerPixel
    val rgbaBytePixels = rgbaBitmap
        .readPixels(dstInfo = imageInfo, dstRowBytes = dstRowBytes, srcX = x, srcY = y)!!

    val argbIntPixels = IntArray(rgbaBytePixels.size / 4)
    for (i in argbIntPixels.indices) {
        val r = rgbaBytePixels[i * 4].toInt() and 0xFF
        val g = rgbaBytePixels[i * 4 + 1].toInt() and 0xFF
        val b = rgbaBytePixels[i * 4 + 2].toInt() and 0xFF
        val a = rgbaBytePixels[i * 4 + 3].toInt() and 0xFF
        argbIntPixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    return argbIntPixels
}

/**
 * Returns the pixel at the specified position in ARGB_8888 format
 */
actual fun Bitmap.readIntPixel(x: Int, y: Int): Int {
    return readIntPixels(x, y, 1, 1).first()
}