package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.TileBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

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