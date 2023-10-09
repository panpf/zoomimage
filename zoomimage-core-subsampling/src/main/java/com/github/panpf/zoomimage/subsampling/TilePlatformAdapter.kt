package com.github.panpf.zoomimage.subsampling

import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.Logger

interface TilePlatformAdapter {

    fun createReuseHelper(logger: Logger, reuseSpec: TileBitmapReuseSpec): TileBitmapReuseHelper

    @WorkerThread
    fun decodeImageInfo(imageSource: ImageSource, ignoreExifOrientation: Boolean): Result<ImageInfo>

    fun checkSupport(mimeType: String): Boolean

    @WorkerThread
    fun createDecoder(
        logger: Logger,
        imageSource: ImageSource,
        imageInfo: ImageInfo,
        reuseHelper: TileBitmapReuseHelper,
    ): TileDecoder
}