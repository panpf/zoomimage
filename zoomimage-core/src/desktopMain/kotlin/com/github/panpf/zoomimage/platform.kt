package com.github.panpf.zoomimage

import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.util.Logger

actual fun createLogPipeline(): Logger.Pipeline = Logger.LogPipeline()

actual fun decodeImageInfo(imageSource: ImageSource): Result<ImageInfo> =
    Result.failure(UnsupportedOperationException("The desktop platform does not support subsampling"))

actual fun decodeExifOrientation(imageSource: ImageSource): Result<ExifOrientation> =
    Result.failure(UnsupportedOperationException("The desktop platform does not support ExifOrientation"))

actual fun checkSupportSubsamplingByMimeType(mimeType: String): Boolean = false

actual fun createTileBitmapReuseHelper(
    logger: Logger,
    tileBitmapReuseSpec: TileBitmapReuseSpec,
): TileBitmapReuseHelper? = null

// todo 尝试用以下文章提到的方法实现桌面平台的子采样
// todo https://github.com/saket/telephoto/issues/9
// todo https://stackoverflow.com/questions/15141539/how-do-i-load-an-enormous-image-to-java-via-bufferedimage/15149382#15149382
actual fun createTileDecoder(
    logger: Logger,
    imageSource: ImageSource,
    imageInfo: ImageInfo,
    tileBitmapReuseHelper: TileBitmapReuseHelper?,
    exifOrientation: ExifOrientation?,
): Result<TileDecoder> = Result.failure(UnsupportedOperationException("The desktop platform does not support subsampling"))