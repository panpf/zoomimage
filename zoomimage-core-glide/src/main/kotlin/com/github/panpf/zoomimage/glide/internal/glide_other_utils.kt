package com.github.panpf.zoomimage.glide.internal

import android.graphics.Bitmap


/**
 * Convert the object to a hexadecimal string
 *
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlideOtherUtilsTest.testToHexString
 */
internal fun Any.toHexString(): String = this.hashCode().toString(16)

/**
 * Get the log string description of Bitmap, it additionally contains the hexadecimal string representation of the Bitmap memory address.
 *
 * @see com.github.panpf.zoomimage.core.glide.test.internal.GlideOtherUtilsTest.testToLogString
 */
internal fun Bitmap.toLogString(): String = "Bitmap@${toHexString()}(${width}x${height},$config)"