package com.github.panpf.zoomimage.subsampling.internal

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build

internal fun isInBitmapError(throwable: Throwable): Boolean =
    if (throwable is IllegalArgumentException) {
        val message = throwable.message.orEmpty()
        (message == "Problem decoding into existing bitmap" || message.contains("bitmap"))
    } else {
        false
    }

internal fun isSrcRectError(throwable: Throwable): Boolean =
    if (throwable is IllegalArgumentException) {
        val message = throwable.message.orEmpty()
        message == "rectangle is outside the image srcRect" || message.contains("srcRect")
    } else {
        false
    }

@SuppressLint("ObsoleteSdkInt")
fun isSupportBitmapRegionDecoder(mimeType: String): Boolean =
    "image/jpeg".equals(mimeType, true)
            || "image/png".equals(mimeType, true)
            || "image/webp".equals(mimeType, true)
            || ("image/heic".equals(mimeType, true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            || ("image/heif".equals(mimeType, true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)