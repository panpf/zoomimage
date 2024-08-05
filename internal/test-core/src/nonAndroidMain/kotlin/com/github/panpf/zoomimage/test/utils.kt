package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.subsampling.SkiaBitmap


/**
 * Convert SkiaBitmap to a log string
 *
 * @see com.github.panpf.zoomimage.core.nonandroid.test.subsampling.SkiasTest.testToLogString
 */
fun SkiaBitmap.toLogString(): String =
    "SkiaBitmap@${hashCode().toString(16)}(${width.toFloat()}x${height.toFloat()},${colorType})"