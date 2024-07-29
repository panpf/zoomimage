package com.github.panpf.zoomimage.core.jvmcommon.test.subsampling

import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromFile
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageSourceJvmCommonTest {

    @Test
    fun testFileImageSource() {
        val file = File("/sdcard/sample.jpeg")
        assertEquals(
            expected = FileImageSource(path = file.toOkioPath()),
            actual = FileImageSource(file = file)
        )
    }

    @Test
    fun testFromFile() {
        val file = File("/sdcard/sample.jpeg")
        assertEquals(
            expected = FileImageSource(path = file.toOkioPath()),
            actual = ImageSource.fromFile(file = file)
        )
    }
}