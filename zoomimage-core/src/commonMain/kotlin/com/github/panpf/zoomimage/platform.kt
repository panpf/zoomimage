package com.github.panpf.zoomimage

import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.util.Logger

expect fun createLogPipeline(): Logger.Pipeline

expect fun decodeImageInfo(imageSource: ImageSource): Result<ImageInfo>

expect fun decodeExifOrientation(imageSource: ImageSource): Result<ExifOrientation>

expect fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean

expect fun createTileBitmapReuseHelper(
    logger: Logger,
    tileBitmapReuseSpec: TileBitmapReuseSpec,
): TileBitmapReuseHelper?

expect fun createTileDecoder(
    logger: Logger,
    imageSource: ImageSource,
    imageInfo: ImageInfo,
    tileBitmapReuseHelper: TileBitmapReuseHelper?,
    exifOrientation: ExifOrientation?,
): Result<TileDecoder>