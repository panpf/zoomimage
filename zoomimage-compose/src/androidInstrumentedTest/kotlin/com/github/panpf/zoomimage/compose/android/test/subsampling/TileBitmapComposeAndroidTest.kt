package com.github.panpf.zoomimage.compose.android.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.asComposeBitmap
import com.github.panpf.zoomimage.test.createBitmap
import kotlin.test.Test

class TileBitmapComposeAndroidTest {

    @Test
    fun testAsComposeBitmap() {
        createBitmap(101, 202).asComposeBitmap()
    }
}