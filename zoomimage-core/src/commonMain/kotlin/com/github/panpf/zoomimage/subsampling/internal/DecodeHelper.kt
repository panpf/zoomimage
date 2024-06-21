@file:Suppress("UnnecessaryVariable")

package com.github.panpf.zoomimage.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntRectCompat
import okio.Closeable

interface DecodeHelper : Closeable {

    val imageInfo: ImageInfo

    val supportRegion: Boolean

    fun decodeRegion(region: IntRectCompat, sampleSize: Int): TileBitmap

    fun copy(): DecodeHelper
}