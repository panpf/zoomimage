package com.github.panpf.zoomimage.test

import android.graphics.BitmapFactory
import com.github.panpf.zoomimage.images.ComposeResImageFile
import com.github.panpf.zoomimage.subsampling.TileBitmap
import okio.buffer

actual suspend fun ComposeResImageFile.decode(): TileBitmap {
    return toImageSource().openSource().buffer().inputStream().use {
        BitmapFactory.decodeStream(it)
    }
}