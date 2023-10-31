package com.github.panpf.zoomimage

import com.github.panpf.zoomimage.subsampling.DesktopTileDecoder
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.isSupportSourceRegion
import com.github.panpf.zoomimage.subsampling.internal.readExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.Logger

actual fun createLogPipeline(): Logger.Pipeline = Logger.LogPipeline()

actual fun decodeImageInfo(imageSource: ImageSource): Result<ImageInfo> =
    imageSource.readImageInfo()

actual fun decodeExifOrientation(imageSource: ImageSource): Result<ExifOrientation> =
    imageSource.readExifOrientation()

actual fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean =
    isSupportSourceRegion(mimeType)

actual fun createTileBitmapReuseHelper(
    logger: Logger,
    tileBitmapReuseSpec: TileBitmapReuseSpec,
): TileBitmapReuseHelper? = null

actual fun createTileDecoder(
    logger: Logger,
    imageSource: ImageSource,
    imageInfo: ImageInfo,
    exifOrientation: ExifOrientation?,
    tileBitmapReuseHelper: TileBitmapReuseHelper?,
): Result<TileDecoder> =
    Result.success(DesktopTileDecoder(logger, imageSource, imageInfo, exifOrientation))