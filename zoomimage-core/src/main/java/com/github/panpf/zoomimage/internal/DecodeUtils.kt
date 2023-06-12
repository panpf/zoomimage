package com.github.panpf.zoomimage.internal

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ImageSource.readImageBounds(): Result<BitmapFactory.Options?> {
    return openInputStream()
        .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
        .use { inputStream ->
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeStream(inputStream, null, this)
                    }.takeIf { it.outWidth > 0 && it.outHeight > 0 }
                }
            }
        }
}

suspend fun ImageSource.readExifOrientation(ignoreExifOrientation: Boolean): Result<Int> {
    val orientationUndefined = ExifInterface.ORIENTATION_UNDEFINED
    return if (!ignoreExifOrientation) {
        openInputStream()
            .let { it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!) }
            .use { inputStream ->
                withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        ExifInterface(inputStream)
                            .getAttributeInt(ExifInterface.TAG_ORIENTATION, orientationUndefined)
                    }
                }
            }
    } else {
        Result.success(orientationUndefined)
    }
}