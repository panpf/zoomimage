package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageSource
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageSourceTest {

    @Test
    fun testFileImageSource() {
        val imageFile = "/sample/sample_cat.jpg"
        ImageSource.fromFile(imageFile).apply {
            assertEquals(imageFile, key)
            assertEquals("FileImageSource('$imageFile')", toString())
        }

        val imageFile2 = "/sample/sample_dog.jpg".toPath()
        ImageSource.fromFile(imageFile2).apply {
            assertEquals(imageFile2.toString(), key)
            assertEquals("FileImageSource('$imageFile2')", toString())
        }
    }
}