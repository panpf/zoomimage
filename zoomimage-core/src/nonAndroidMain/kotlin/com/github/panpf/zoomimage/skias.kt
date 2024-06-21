package com.github.panpf.zoomimage

typealias SkiaBitmap = org.jetbrains.skia.Bitmap
typealias SkiaCanvas = org.jetbrains.skia.Canvas
typealias SkiaRect = org.jetbrains.skia.Rect
typealias SkiaImage = org.jetbrains.skia.Image


internal fun SkiaBitmap.toLogString(): String {
    return "SkiaBitmap@${hashCode().toString(16)}(${width.toFloat()}x${height.toFloat()},${colorType})"
}