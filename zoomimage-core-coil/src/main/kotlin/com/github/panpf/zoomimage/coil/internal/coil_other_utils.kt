package com.github.panpf.zoomimage.coil.internal

import android.graphics.Bitmap


internal fun Any.toHexString(): String = this.hashCode().toString(16)

internal fun Bitmap.toLogString(): String = "Bitmap@${toHexString()}(${width}x${height},$config)"