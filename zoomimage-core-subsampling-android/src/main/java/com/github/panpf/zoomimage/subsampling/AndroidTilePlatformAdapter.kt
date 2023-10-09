package com.github.panpf.zoomimage.subsampling

import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.subsampling.internal.isSupportBitmapRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo

open class AndroidTilePlatformAdapter : TilePlatformAdapter {

    override fun createReuseHelper(
        logger: Logger,
        reuseSpec: TileBitmapReuseSpec
    ): TileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, reuseSpec)

    @WorkerThread
    override fun decodeImageInfo(
        imageSource: ImageSource,
        ignoreExifOrientation: Boolean
    ): Result<ImageInfo> = imageSource.readImageInfo(ignoreExifOrientation)

    override fun checkSupport(mimeType: String): Boolean = isSupportBitmapRegionDecoder(mimeType)

    @WorkerThread
    override fun createDecoder(
        logger: Logger,
        imageSource: ImageSource,
        imageInfo: ImageInfo,
        reuseHelper: TileBitmapReuseHelper,
    ): TileDecoder {
        return AndroidTileDecoder(
            logger = logger,
            imageInfo = imageInfo,
            imageSource = imageSource,
            reuseHelper = reuseHelper as AndroidTileBitmapReuseHelper,
        )
    }
}