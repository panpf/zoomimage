package com.github.panpf.zoomimage.test

import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import okio.buffer

@WorkerThread
fun ImageSource.decodeExifOrientation(): Result<Int> = runCatching {
    val source = openSource()
    val inputStream = source.buffer().inputStream()
    val exifOrientation = inputStream.use {
        ExifInterface(it).getAttributeInt(
            /* tag = */ ExifInterface.TAG_ORIENTATION,
            /* defaultValue = */ ExifInterface.ORIENTATION_UNDEFINED
        )
    }
    exifOrientation
}


/**
 * @see [com.github.panpf.zoomimage.core.test.subsampling.internal.AndroidTileDecodeUtilsTest.testReadImageInfo]
 */
fun ImageSource.decodeImageInfo(): Result<ImageInfo> = runCatching {
    val source = openSource()
    val inputStream = source.buffer().inputStream()
    val options = inputStream.use {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(it, null, options)
        options
    }
    val size = IntSizeCompat(options.outWidth, options.outHeight)
    val imageInfo = ImageInfo(size, options.outMimeType.orEmpty())
    imageInfo
}