package com.github.panpf.zoomimage.test

import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat

@WorkerThread
fun ImageSource.decodeExifOrientation(): Result<AndroidExifOrientation> {
    val inputStreamResult = openSource()
    if (inputStreamResult.isFailure) {
        return Result.failure(inputStreamResult.exceptionOrNull()!!)
    }
    val inputStream = inputStreamResult.getOrNull()!!
    val exifOrientation = try {
        inputStream.use {
            ExifInterface(it).getAttributeInt(
                /* tag = */ ExifInterface.TAG_ORIENTATION,
                /* defaultValue = */ ExifInterface.ORIENTATION_UNDEFINED
            )
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }
    return Result.success(AndroidExifOrientation(exifOrientation))
}


/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testReadImageInfo]
 */
fun ImageSource.decodeImageInfo(): Result<ImageInfo> {
    val inputStreamResult = openSource()
    if (inputStreamResult.isFailure) {
        return Result.failure(inputStreamResult.exceptionOrNull()!!)
    }
    val inputStream = inputStreamResult.getOrNull()!!
    val options = try {
        inputStream.use {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(it, null, options)
            options
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }
    val size = IntSizeCompat(options.outWidth, options.outHeight)
    val imageInfo = ImageInfo(size, options.outMimeType.orEmpty())
    return Result.success(imageInfo)
}