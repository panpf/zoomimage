package com.github.panpf.zoomimage.test

import android.graphics.BitmapFactory
import com.githb.panpf.zoomimage.images.ResourceImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap
import okio.buffer

actual fun ResourceImageFile.decode(): TileBitmap {
    return toImageSource().openSource().buffer().inputStream().use {
        BitmapFactory.decodeStream(it)
    }
}